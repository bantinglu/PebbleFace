#include <pebble.h>
#define SOME_NUMBER 10
#define ACCEL_SAMPLE_RATE    ACCEL_SAMPLING_10HZ

static Window *s_main_window;
static TextLayer *s_time_layer;
static bool isCapturing = false;

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

static void accel_data_callback(void * data, uint32_t num_samples)
{
  AppMessageResult result;
  AccelData * vector = (AccelData*) data;
  APP_LOG(APP_LOG_LEVEL_INFO, "X: %d", (int) vector->x);
  APP_LOG(APP_LOG_LEVEL_INFO, "Y: %d", (int) vector->y);
  APP_LOG(APP_LOG_LEVEL_INFO, "Y: %d", (int) vector->z);
}
static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  // A single click has just occured
  APP_LOG(APP_LOG_LEVEL_INFO, "Button clicked", NULL);
  isCapturing  = !isCapturing;
  APP_LOG(APP_LOG_LEVEL_INFO, "Capture Mode: %s", isCapturing ? "true" : "false");
  if(isCapturing)
  {
    accel_data_service_subscribe(1, (AccelDataHandler) accel_data_callback);
  }
  else 
  {
    accel_data_service_unsubscribe();
  }
}

static void click_config_provider(void *context) {
  // Subcribe to button click events here
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
}

static void accel_tap_callback(AccelAxisType axis, uint32_t direction)
{
  APP_LOG(APP_LOG_LEVEL_INFO, "In Tap CallBacks", NULL);
}

static void capture()
{
    accel_service_set_sampling_rate(ACCEL_SAMPLE_RATE );  
    accel_tap_service_subscribe((AccelTapHandler) accel_tap_callback);
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
}

static void deinit()
{
   window_destroy(s_main_window);
}

int main(void)
{
  APP_LOG(APP_LOG_LEVEL_INFO, "main: entry:  %s %s", __TIME__, __DATE__);
  init();
  capture();
  app_event_loop();
  deinit();
}