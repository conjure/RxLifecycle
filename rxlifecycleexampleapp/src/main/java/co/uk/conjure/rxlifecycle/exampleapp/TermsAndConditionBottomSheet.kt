package co.uk.conjure.rxlifecycle.exampleapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import co.uk.conjure.rxlifecycle.exampleapp.databinding.FragmentTermsAndConditionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import uk.co.conjure.rxlifecycle.RxView
import uk.co.conjure.rxlifecycle.whileStarted

class TermsAndConditionBottomSheet : BottomSheetDialogFragment() {

    private val termsViewModel: LoginViewModelImpl by activityViewModels()
    lateinit var termsAndConditionsView: TermsAndConditionsView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTermsAndConditionsBinding.inflate(inflater, container, false)
        termsAndConditionsView = TermsAndConditionsView().apply {
            viewModel = termsViewModel
            registerBinding(binding, this@TermsAndConditionBottomSheet)
        }
        return binding.root
    }
}

class TermsAndConditionsView : RxView<FragmentTermsAndConditionsBinding>() {
    lateinit var viewModel: TermsAndConditionsViewModel
    override fun onStart() {
        super.onStart()
        binding.tvTerms.bind(viewModel.termsContent)
        viewModel.isLoadingTerms.whileStarted(this, { isLoading ->
            binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
        viewModel.loadingTermsFailed.whileStarted(this, { hasError ->
            if (hasError) binding.tvTerms.text = "Something went wrong"
        })
    }
}