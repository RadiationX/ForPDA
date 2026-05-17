package forpdateam.ru.forpda.ui.fragments.auth

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AlertDialog
import com.github.rahatarmanahmed.cpv.CircularProgressView
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.simple.SimpleAnimationListener
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher
import forpdateam.ru.forpda.databinding.FragmentAuthBinding
import forpdateam.ru.forpda.entity.remote.auth.AuthCaptcha
import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.presentation.auth.AuthPresenter
import forpdateam.ru.forpda.presentation.auth.AuthView
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.tabBinding

/**
 * Created by radiationx on 29.07.16.
 */
class AuthFragment : TabFragment(R.layout.fragment_auth), AuthView {

    private val binding by tabBinding(FragmentAuthBinding::bind)

    private val nick: EditText
        get() = binding.authLogin
    private val password: EditText
        get() = binding.authPassword
    private val captcha: EditText
        get() = binding.authCaptcha
    private val captchaImage: ImageView
        get() = binding.captchaImage
    private val avatar: ImageView
        get() = binding.authAvatar
    private val sendButton: Button
        get() = binding.authSend
    private val skipButton: Button
        get() = binding.authSkip
    private val regButton: Button
        get() = binding.authReg
    private val loginProgress: ProgressBar
        get() = binding.loginProgress
    private val captchaProgress: ProgressBar
        get() = binding.captchaProgress
    private val hiddenAuth: CheckBox
        get() = binding.authHidden

    private val mainForm: LinearLayout
        get() = binding.authMainForm
    private val complete: RelativeLayout
        get() = binding.authComplete
    private val completeText: TextView
        get() = binding.authCompleteText
    private val progressView: CircularProgressView
        get() = binding.authProgress

    private val authTopButtons: FrameLayout
        get() = binding.authTopButtons


    private val loginTextWatcher = object : SimpleTextWatcher() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            updateForm()
        }
    }

    private val presenter by quillMoxyPresenter<AuthPresenter>()

    init {
        configuration.defaultTitle = getString(R.string.fragment_title_auth)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListsBackground()
        skipButton.setOnClickListener { v ->
            AlertDialog.Builder(requireContext())
                .setMessage("Без авторизации будут недоступны некоторые функции приложения.")
                .setPositiveButton(R.string.ok) { dialog, which -> presenter.onClickSkip() }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
        regButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage("Процесс регистрации включает в себя множество шагов, поэтому рекомендуем зарегистрироваться через браузер.")
                .setPositiveButton(R.string.ok) { _, _ -> presenter.onRegistrationClick() }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
        appBarLayout.visibility = View.GONE
        sendButton.setOnClickListener { v -> tryLogin() }
        nick.addTextChangedListener(loginTextWatcher)
        password.addTextChangedListener(loginTextWatcher)
        captcha.addTextChangedListener(loginTextWatcher)
        fragmentContainer.fitsSystemWindows = true
        fragmentContent.fitsSystemWindows = true

        hiddenAuth.setOnCheckedChangeListener { buttonView, isChecked ->
            updateForm()
        }

        captcha.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (sendButton.isEnabled) {
                    tryLogin()
                }
                return@OnEditorActionListener true
            }
            false
        })
    }

    override fun setSendEnabled(isEnabled: Boolean) {
        sendButton.isEnabled = isEnabled
    }

    override fun setSendRefreshing(isRefreshing: Boolean) {
        if (isRefreshing) {
            loginProgress.visibility = View.VISIBLE
            sendButton.visibility = View.INVISIBLE
        } else {
            loginProgress.visibility = View.INVISIBLE
            sendButton.visibility = View.VISIBLE
        }
    }

    override fun onCaptchaLoaded(authCaptcha: AuthCaptcha) {
        captchaImage.visibility = View.GONE
        captchaProgress.visibility = View.VISIBLE
        ImageLoader.getInstance().displayImage(
            authCaptcha.captchaImageUrl,
            captchaImage,
            object : SimpleImageLoadingListener() {
                override fun onLoadingComplete(
                    imageUri: String?,
                    view: View?,
                    loadedImage: Bitmap?
                ) {
                    captchaImage.visibility = View.VISIBLE
                    captchaProgress.visibility = View.GONE
                }
            })
    }

    override fun onFormChanged(authForm: AuthForm) {
        nick.setText(authForm.nick)
        password.setText(authForm.password)
        captcha.setText(authForm.captcha)
        hiddenAuth.isChecked = authForm.isHidden
    }

    private fun tryLogin() {
        hideKeyboard()
        presenter.signIn()
    }

    override fun onSuccessAuth() {
        mainForm.startAnimation(AlphaAnimation(1.0f, 0.0f).apply {
            duration = 225
            setAnimationListener(object : SimpleAnimationListener() {
                override fun onAnimationEnd(animation: Animation) {
                    mainForm.visibility = View.GONE
                }
            })
        })
        authTopButtons.startAnimation(AlphaAnimation(1.0f, 0.0f).apply {
            duration = 225
            setAnimationListener(object : SimpleAnimationListener() {
                override fun onAnimationEnd(animation: Animation) {
                    authTopButtons?.visibility = View.GONE
                }
            })
        })
        complete.visibility = View.VISIBLE
        complete.startAnimation(AlphaAnimation(0.0f, 1.0f).apply {
            duration = 375
        })
    }

    override fun showProfile(profile: ProfileModel) {
        ImageLoader.getInstance().displayImage(profile.user.avatar, avatar)
        completeText.text = ApiUtils.spannedFromHtml("${getString(R.string.auth_hello)}, <b>${profile.user.nick}</b>!")
        completeText.visibility = View.VISIBLE

        completeText.startAnimation(AlphaAnimation(0.0f, 1.0f).apply {
            duration = 1000
        })

        progressView.startAnimation(AlphaAnimation(1.0f, 0.0f).apply {
            duration = 225
            setAnimationListener(object : SimpleAnimationListener() {
                override fun onAnimationEnd(animation: Animation) {
                    progressView.visibility = View.GONE
                    progressView.stopAnimation()
                }
            })
        })
    }

    private fun updateForm() {
        presenter.updateForm(
            AuthForm(
                nick = nick.text?.toString().orEmpty(),
                password = password.text?.toString().orEmpty(),
                captcha = captcha.text?.toString().orEmpty(),
                isHidden = hiddenAuth.isChecked
            )
        )
    }
}
