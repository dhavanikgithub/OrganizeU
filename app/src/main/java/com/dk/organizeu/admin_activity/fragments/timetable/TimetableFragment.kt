package com.dk.organizeu.admin_activity.fragments.timetable

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.dk.organizeu.R
import com.dk.organizeu.databinding.FragmentTimetableBinding
import com.dk.organizeu.utils.CustomProgressDialog

class TimetableFragment : Fragment() {

    companion object {
        fun newInstance() = TimetableFragment()
    }

    private lateinit var viewModel: TimetableViewModel
    private lateinit var binding: FragmentTimetableBinding
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_timetable, container, false)
        binding = FragmentTimetableBinding.bind(view)
        viewModel = ViewModelProvider(this)[TimetableViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {

            }
        }
    }
}