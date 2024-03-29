package com.dk.organizeu.admin_activity.fragments.timetable.add_timetable

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.dialog_box.AddLessonDialog
import com.dk.organizeu.admin_activity.dialog_box.AddSubjectDialog
import com.dk.organizeu.databinding.FragmentAddTimetableBinding
import com.dk.organizeu.utils.CustomProgressDialog

class AddTimetableFragment : Fragment() {

    companion object {
        fun newInstance() = AddTimetableFragment()
    }

    private lateinit var viewModel: AddTimetableViewModel
    private lateinit var binding: FragmentAddTimetableBinding
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_timetable, container, false)
        binding = FragmentAddTimetableBinding.bind(view)
        viewModel = ViewModelProvider(this)[AddTimetableViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                btnAddLesson.setOnClickListener {
                    val dialogFragment = AddLessonDialog()
                    dialogFragment.show(childFragmentManager, "customDialog")
                }
            }
        }
    }
}