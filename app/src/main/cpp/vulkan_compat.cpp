#include <vulkan/vulkan.h>

extern "C" VKAPI_ATTR void VKAPI_CALL vkGetPhysicalDeviceFeatures2(
    VkPhysicalDevice physical_device,
    VkPhysicalDeviceFeatures2* features) {
    vkGetPhysicalDeviceFeatures2KHR(physical_device, features);
}
