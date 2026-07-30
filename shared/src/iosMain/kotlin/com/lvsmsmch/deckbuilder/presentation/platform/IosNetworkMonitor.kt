package com.lvsmsmch.deckbuilder.presentation.platform

import kotlin.concurrent.Volatile
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Network.nw_interface_type_cellular
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_uses_interface_type
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_get_global_queue

/**
 * NWPathMonitor-backed reachability: keeps the latest path state so the
 * mobile-data download gate works the same way it does on Android.
 */
@OptIn(ExperimentalForeignApi::class)
class IosNetworkMonitor : NetworkMonitor {

    @Volatile
    private var current: NetworkType = NetworkType.Wifi

    init {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_update_handler(monitor) { path ->
            current = when {
                nw_path_get_status(path) != nw_path_status_satisfied -> NetworkType.None
                nw_path_uses_interface_type(path, nw_interface_type_cellular) -> NetworkType.Mobile
                else -> NetworkType.Wifi
            }
        }
        nw_path_monitor_set_queue(
            monitor,
            dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u),
        )
        nw_path_monitor_start(monitor)
    }

    override fun currentNetworkType(): NetworkType = current
}
