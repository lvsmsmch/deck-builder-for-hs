package com.lvsmsmch.deckbuilder.presentation.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.UIKit.UIWindow
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

/**
 * Android-style toast rendered as a UIKit overlay on the key window, so it
 * floats above the Compose hierarchy and any dialogs. Fade in, hold ~2.5s
 * (matching the app's snackbar duration), fade out.
 */
class IosToaster : Toaster {

    override fun show(message: String) {
        dispatch_async(dispatch_get_main_queue()) { present(message) }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun present(message: String) {
        val window = UIApplication.sharedApplication.windows
            .filterIsInstance<UIWindow>()
            .firstOrNull { it.keyWindow }
            ?: UIApplication.sharedApplication.keyWindow
            ?: return

        val (winW, winH) = window.bounds.useContents { size.width to size.height }

        val label = UILabel().apply {
            text = message
            textColor = UIColor.whiteColor
            font = UIFont.systemFontOfSize(14.0)
            textAlignment = NSTextAlignmentCenter
            numberOfLines = 3
        }
        val maxTextWidth = winW - 2 * H_MARGIN - 2 * H_PADDING
        val textSize = label.sizeThatFits(CGSizeMake(maxTextWidth, 200.0))
        val (textW, textH) = textSize.useContents { width to height }

        val toastW = textW + 2 * H_PADDING
        val toastH = textH + 2 * V_PADDING
        val container = UIView().apply {
            backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.85)
            layer.cornerRadius = toastH / 2.0
            clipsToBounds = true
            alpha = 0.0
            userInteractionEnabled = false
            setFrame(
                CGRectMake(
                    (winW - toastW) / 2.0,
                    winH - toastH - BOTTOM_OFFSET,
                    toastW,
                    toastH,
                ),
            )
        }
        label.setFrame(CGRectMake(H_PADDING, V_PADDING, textW, textH))
        container.addSubview(label)
        window.addSubview(container)

        UIView.animateWithDuration(0.2, animations = { container.alpha = 1.0 }) { _ ->
            val hideAt = dispatch_time(DISPATCH_TIME_NOW, HOLD_NANOS)
            dispatch_after(hideAt, dispatch_get_main_queue()) {
                UIView.animateWithDuration(0.25, animations = { container.alpha = 0.0 }) { _ ->
                    container.removeFromSuperview()
                }
            }
        }
    }

    private companion object {
        const val H_MARGIN = 40.0
        const val H_PADDING = 18.0
        const val V_PADDING = 12.0
        const val BOTTOM_OFFSET = 120.0
        const val HOLD_NANOS = 2_500_000_000L
    }
}
