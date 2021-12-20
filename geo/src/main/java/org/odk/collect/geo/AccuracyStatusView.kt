package org.odk.collect.geo

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue
import org.odk.collect.geo.databinding.AccuracyStatusBinding
import java.text.DecimalFormat

class AccuracyStatusView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    var binding = AccuracyStatusBinding.inflate(LayoutInflater.from(context), this, true)
        private set

    fun setAccuracy(accuracy: Float, accuracyThreshold: Float) {
        val (backgroundColor, textColor) = getBackgroundAndTextColor(accuracy)
        binding.root.background = ColorDrawable(backgroundColor)
        binding.qualitative.setTextColor(textColor)
        binding.action.setTextColor(textColor)
        binding.currentAccuracy.setTextColor(textColor)

        binding.currentAccuracy.text = formatAccuracy(accuracy)
        binding.qualitative.text = context.getString(
            R.string.distance_from_accuracy_goal,
            formatAccuracy(accuracy - accuracyThreshold),
            formatAccuracy(accuracyThreshold)
        )
    }

    private fun getBackgroundAndTextColor(accuracy: Float): Pair<Int, Int> {
        return if (accuracy >= 100) {
            Pair(
                getThemeAttributeValue(context, R.attr.colorError),
                getThemeAttributeValue(context, R.attr.colorOnError)
            )
        } else {
            Pair(
                getThemeAttributeValue(context, R.attr.colorPrimary),
                getThemeAttributeValue(context, R.attr.colorOnPrimary)
            )
        }
    }

    private fun formatAccuracy(accuracy: Float) =
        "${DecimalFormat("#.##").format(accuracy)}m"
}
