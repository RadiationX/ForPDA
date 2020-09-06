package forpdateam.ru.forpda.common.simple

import android.view.animation.Animation

open class SimpleAnimationListener : Animation.AnimationListener {
    override fun onAnimationRepeat(animation: Animation) = Unit
    override fun onAnimationEnd(animation: Animation) = Unit
    override fun onAnimationStart(animation: Animation) = Unit
}