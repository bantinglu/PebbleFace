#include <pebble.h>

static Window *s_main_window;
static bool isCapturing = false;
static TextLayer *s_time_layer;
static DictionaryIterator *iter;
static bool isBlocked = false;

enum Pebble_Keys 
{
  PP_KEY_CMD  = 128,
  PP_KEY_DIR    = 1,
  PP_KEY_MAG    = 2,
};

enum PebblePointer_Cmd_Values
{
  PP_CMD_INVALID = 0,
  PP_CMD_VECTOR  = 1,
};

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
  text_layer_set_text(s_time_layer, "Force - Tap APP");
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

static void accel_data_callback(AccelAxisType axis, int32_t direction)
{
  int axel = 0;
  if(axis == ACCEL_AXIS_X) {
    APP_LOG(APP_LOG_LEVEL_INFO, "Axis: X   Direction: %d", (int) direction);
    axel = 1;
  }
  else if (axis == ACCEL_AXIS_Y) {
    APP_LOG(APP_LOG_LEVEL_INFO, "Axis: Y   Direction: %d", (int) direction);
    axel = 2;
  }
  else if (axis == ACCEL_AXIS_Z) {
    APP_LOG(APP_LOG_LEVEL_INFO, "Axis: Z   Direction: %d", (int) direction); 
    axel = 3;
  }
  
  if(isBlocked)
    return;
  
  // Declare the dictionary's iterator
  
  // Prepare the outbox buffer for this message
  AppMessageResult result = app_message_outbox_begin(&iter);
  if(result == APP_MSG_OK) {
    
    Tuplet vector[] = {TupletInteger(PP_KEY_CMD, PP_CMD_VECTOR),
                       TupletInteger(PP_KEY_DIR, axel ),
                       TupletInteger(PP_KEY_MAG, (int) direction)};
  
    dict_write_tuplet(iter, &vector[0]);
    dict_write_tuplet(iter, &vector[1]);
    dict_write_tuplet(iter, &vector[2]);

  
    // Send this message
    result = app_message_outbox_send();
    
    if(result != APP_MSG_OK)
    {
      APP_LOG(APP_LOG_LEVEL_ERROR, "Error sending the outbox: %d", (int)result);
    }
    else if(result == APP_MSG_OK)
    {
      APP_LOG(APP_LOG_LEVEL_INFO, "send waiting for callback");
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
    accel_tap_service_subscribe((AccelTapHandler) accel_data_callback);
  }
  else 
  {
    accel_tap_service_unsubscribe();
    //send_end_ack();
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
  const uint32_t outbound_size = 1024;
  app_message_open(inbound_size, outbound_size);
  app_comm_set_sniff_interval(SNIFF_INTERVAL_REDUCED);
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