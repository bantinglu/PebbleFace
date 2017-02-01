#include <pebble.h>

#define ACCEL_SAMPLE_RATE    ACCEL_SAMPLING_10HZ
#define SAMPLES_PER_CALLBACK  1

#define SYNC_BUFFER_SIZE      48

static const int DONE_KEY = 64;

static Window *s_main_window;
static bool isCapturing = false;
static TextLayer *s_time_layer;

enum Pebble_Keys 
{
  PP_KEY_CMD  = 128,
  PP_KEY_X    = 1,
  PP_KEY_Y    = 2,
  PP_KEY_Z    = 3,
};

enum PebblePointer_Cmd_Values
{
  PP_CMD_INVALID = 0,
  PP_CMD_VECTOR  = 1,
};

static bool isBlocked = false;

void out_sent_handler(DictionaryIterator *sent, void *context) 
{
  // outgoing message was delivered
  isBlocked = false;
  APP_LOG(APP_LOG_LEVEL_INFO, "Booyah Baby!: outbox");
}


void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context)
{
  // outgoing message failed
  APP_LOG(APP_LOG_LEVEL_ERROR, "Message Sent Failed. Reason: %d", (int)reason);
}


void in_received_handler(DictionaryIterator *received, void *context) 
{
  // incoming message received
  APP_LOG(APP_LOG_LEVEL_INFO, "Booyah Baby! : inbox");
}


void in_dropped_handler(AppMessageResult reason, void *context) 
{
  // incoming message dropped
  APP_LOG(APP_LOG_LEVEL_ERROR, "Message dropped. Reason: %d", (int)reason);
}

static void main_window_load(Window *window)
{
  // Get information about the Window
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

  // Create the TextLayer with specific bounds
  s_time_layer = text_layer_create(
      GRect(0, PBL_IF_ROUND_ELSE(58, 52), bounds.size.w, 50));

  // Improve the layout to be more like a watchface
  text_layer_set_background_color(s_time_layer, GColorClear);
  text_layer_set_text_color(s_time_layer, GColorBlack);
  text_layer_set_text(s_time_layer, "Banting's APP");
  text_layer_set_font(s_time_layer, fonts_get_system_font(FONT_KEY_GOTHIC_14));
  text_layer_set_text_alignment(s_time_layer, GTextAlignmentCenter);
 

  // Add it as a child layer to the Window's root layer
  layer_add_child(window_layer, text_layer_get_layer(s_time_layer));
}


static void main_window_unload(Window *window)
{
  // Destroy TextLayer
  text_layer_destroy(s_time_layer);
}

static void accel_data_callback(void * data, uint32_t num_samples)
{
  AccelData *accel = (AccelData*) data;
  
  if(isBlocked)
    return;
  
  // Declare the dictionary's iterator
  DictionaryIterator *iter;
  
  // Prepare the outbox buffer for this message
  AppMessageResult result = app_message_outbox_begin(&iter);
  if(result == APP_MSG_OK) {
    
    Tuplet vector[] = {TupletInteger(PP_KEY_CMD, PP_CMD_VECTOR),
                       TupletInteger(PP_KEY_X, (int)accel->x),
                       TupletInteger(PP_KEY_Y, (int)accel->y),
                       TupletInteger(PP_KEY_Z, (int)accel->z)};
  
    dict_write_tuplet(iter, &vector[0]);
    dict_write_tuplet(iter, &vector[PP_KEY_X]);
    dict_write_tuplet(iter, &vector[PP_KEY_Y]);
    dict_write_tuplet(iter, &vector[PP_KEY_Z]);

  
    // Send this message
    result = app_message_outbox_send();
    
    if(result != APP_MSG_OK)
    {
      APP_LOG(APP_LOG_LEVEL_ERROR, "Error sending the outbox: %d", (int)result);
    }
    else if(result == APP_MSG_OK)
    {
      APP_LOG(APP_LOG_LEVEL_INFO, "send waiting for callabck");
      isBlocked = true;
    }
  } 
  else 
  {
    // The outbox cannot be used right now
    APP_LOG(APP_LOG_LEVEL_ERROR, "Error preparing the outbox: %d", (int)result);
  }
} 


void send_end_ack()
{
  DictionaryIterator *iter;

  // Prepare the outbox buffer for this message
  
 
  APP_LOG(APP_LOG_LEVEL_INFO,"Is Blocked: %s", isBlocked ? "true" : "false");
  
  
  do
  {
    AppMessageResult result = app_message_outbox_begin(&iter);
  } while(result != APP_MSG_OK);
    
  if(result == APP_MSG_OK) 
  {  
    dict_write_int(iter, PP_KEY_CMD, &DONE_KEY, sizeof(int), true);
    result = app_message_outbox_send();
    
    if(result != APP_MSG_OK)
    {
      APP_LOG(APP_LOG_LEVEL_ERROR, "Error sending the outbox: %d", (int)result);
    }
    else if(result == APP_MSG_OK)
    {
      APP_LOG(APP_LOG_LEVEL_INFO, "send waiting for callabck");
      isBlocked = true;
    }
  }
  else 
  {
    // The outbox cannot be used right now
    APP_LOG(APP_LOG_LEVEL_ERROR, "Error preparing the outbox: %d", (int)result);
  }
}

static void select_click_handler(ClickRecognizerRef recognizer, void *context) 
{
  isCapturing  = !isCapturing;
  APP_LOG(APP_LOG_LEVEL_INFO, "Capture Mode: %s", isCapturing ? "true" : "false");
  if(isCapturing)
  {
    accel_data_service_subscribe(1, (AccelDataHandler) accel_data_callback);
  }
  else 
  {
    accel_data_service_unsubscribe();
    send_end_ack();
  }
}

static void click_config_provider(void *context) 
{
  // Subcribe to button click events here
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
}


static void init()
{
  // Create main Window element and assign to pointer
  s_main_window = window_create();

  // Set handlers to manage the elements inside the Window
  window_set_window_handlers(s_main_window, (WindowHandlers) {
    .load = main_window_load,
    .unload = main_window_unload
  });

  // Show the Window on the watch, with animated=true
  window_stack_push(s_main_window, true);
  
  window_set_click_config_provider(s_main_window, (ClickConfigProvider) click_config_provider);
  
  app_message_register_inbox_received(in_received_handler);
  app_message_register_inbox_dropped(in_dropped_handler);
  app_message_register_outbox_sent(out_sent_handler);
  app_message_register_outbox_failed(out_failed_handler);
  const uint32_t inbound_size = 128;
  const uint32_t outbound_size = 128;
  app_message_open(inbound_size, outbound_size);
}

static void deinit()
{
   window_destroy(s_main_window);
}

int main(void)
{
  APP_LOG(APP_LOG_LEVEL_INFO, "main: entry:  %s %s", __TIME__, __DATE__);
  init();
  app_event_loop();
  deinit();
}