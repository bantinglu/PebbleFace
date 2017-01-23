#include <pebble.h>

#define ACCEL_SAMPLE_RATE    ACCEL_SAMPLING_10HZ

static const uint32_t MESSAGE_KEY_RequestData = 0x00;
static Window *s_main_window;

static TextLayer *s_time_layer;

enum Pebble_Keys {
  PP_KEY_CMD  = 128,
  PP_KEY_X    = 1,
  PP_KEY_Y    = 2,
  PP_KEY_Z    = 3,
};

static char * AppMessageResult_to_String(AppMessageResult error)
{
  switch (error) {
    case APP_MSG_OK:                          return "OK";
    case APP_MSG_SEND_TIMEOUT:                return "SEND_TIMEOUT";
    case APP_MSG_NOT_CONNECTED:               return "NOT_CONNECTED";
    case APP_MSG_APP_NOT_RUNNING:             return "APP_NOT_RUNNING";
    case APP_MSG_INVALID_ARGS:                return "INVALID_ARGS";
    case APP_MSG_BUSY:                        return "BUSY";
    case APP_MSG_BUFFER_OVERFLOW:             return "BUFFER_OVERFLOW";
    case APP_MSG_ALREADY_RELEASED:            return "ALREADY_RELEASED";
    case APP_MSG_CALLBACK_ALREADY_REGISTERED: return "CALLBACK_ALREADY_REGISTERED";
    case APP_MSG_CALLBACK_NOT_REGISTERED:     return "CALLBACK_NOT_REGISTERED";
    case APP_MSG_OUT_OF_MEMORY:               return "OUT_OF_MEMORY";
    case APP_MSG_CLOSED:                      return "CLOSED";
    case APP_MSG_INTERNAL_ERROR:              return "INTERNAL_ERROR";
    default:                                  return "unknown";
  }
}

void out_sent_handler(DictionaryIterator *sent, void *context) {
  // outgoing message was delivered
}


void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
  // outgoing message failed
}


void in_received_handler(DictionaryIterator *received, void *context) {
  // incoming message received
}


void in_dropped_handler(AppMessageResult reason, void *context) {
  // incoming message dropped
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
  layer_add_child(window_layer, text_layer_get_layer(s_time_layer));}

static void main_window_unload(Window *window)
{
  // Destroy TextLayer
  text_layer_destroy(s_time_layer);
}

static void send()
{
  
  //connection_service_subscribe(connection_handler_callback);
  bool connected = connection_service_peek_pebblekit_connection();
  APP_LOG(APP_LOG_LEVEL_INFO, "%d", connected);
  
  // Declare the dictionary's iterator
  DictionaryIterator i;
  DictionaryIterator *iter = &i;
  
  // Prepare the outbox buffer for this message
  AppMessageResult result = app_message_outbox_begin(&iter);
  if(result == APP_MSG_OK) {
    int value = 0;
    dict_write_int(iter, MESSAGE_KEY_RequestData, &value, sizeof(int), true);
  
    // Send this message
    result = app_message_outbox_send();
    
    if(result != APP_MSG_OK)
    {
      APP_LOG(APP_LOG_LEVEL_ERROR, "Error sending the outbox: %d", (int)result);
    }
  } 
  else 
  {
    // The outbox cannot be used right now
    APP_LOG(APP_LOG_LEVEL_ERROR, "Error preparing the outbox: %d", (int)result);
  }
}

static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
 send();
}

static void click_config_provider(void *context) {
  // Subcribe to button click events here
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
}

static void capture()
{
    accel_service_set_sampling_rate(ACCEL_SAMPLE_RATE );  
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
  const uint32_t inbound_size = 64;
  const uint32_t outbound_size = 64;
  app_message_open(inbound_size, outbound_size);
}

static void deinit()
{
   window_destroy(s_main_window);
}

int main(void)
{
  APP_LOG(APP_LOG_LEVEL_INFO, "main: entry:  %s %s", __TIME__, __DATE__);
  init();;
  app_event_loop();
  deinit();
}