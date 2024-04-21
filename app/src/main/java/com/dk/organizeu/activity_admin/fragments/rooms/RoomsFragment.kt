package com.dk.organizeu.activity_admin.fragments.rooms

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.adapter.RoomAdapter
import com.dk.organizeu.activity_admin.dialog.AddRoomDialog
import com.dk.organizeu.databinding.FragmentRoomsBinding
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.repository.RoomRepository
import com.dk.organizeu.repository.RoomRepository.Companion.roomDocumentToRoomObj
import com.dk.organizeu.utils.CustomProgressDialog
import com.dk.organizeu.utils.UtilFunction.Companion.hideProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.showProgressBar
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class RoomsFragment : Fragment(), AddDocumentListener, OnItemClickListener {

    companion object {
        fun newInstance() = RoomsFragment()
        const val TAG = "OrganizeU-RoomsFragment"
    }

    private lateinit var viewModel: RoomsViewModel
    private lateinit var binding: FragmentRoomsBinding
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var db: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_rooms, container, false)
        binding = FragmentRoomsBinding.bind(view)
        viewModel = ViewModelProvider(this)[RoomsViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        db= FirebaseFirestore.getInstance()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {
                try {
                   initRecyclerView()
                } catch (e: Exception) {
                   Log.e(TAG,e.message.toString())
                   requireContext().unexpectedErrorMessagePrint(e)
                }
                swipeRefresh.setOnRefreshListener {
                    initRecyclerView()
                    swipeRefresh.isRefreshing=false
                }

                btnAddRoom.setOnClickListener {
                    try {
                        val dialogFragment = AddRoomDialog()
                        dialogFragment.show(childFragmentManager, "customDialog")
                    } catch (e: Exception) {
                        Log.e(TAG,e.message.toString())
                        requireContext().unexpectedErrorMessagePrint(e)
                    }
                }
            }
        }
    }

    private fun initRecyclerView()
    {
        binding.apply {
            viewModel.apply {
                try {
                    showProgressBar(rvRooms,progressBar)
                    MainScope().launch(Dispatchers.IO){
                        try {
                            roomPojoList.clear()
                            val documents = RoomRepository.getAllRoomDocument()
                            for(document in documents)
                            {
                                val roomItem = roomDocumentToRoomObj(document)
                                roomPojoList.add(roomItem)
                            }
                            withContext(Dispatchers.Main)
                            {
                                try {
                                    roomAdapter = RoomAdapter(roomPojoList,this@RoomsFragment)
                                    rvRooms.layoutManager = LinearLayoutManager(requireContext())
                                    rvRooms.adapter = roomAdapter
                                    delay(500)
                                    hideProgressBar(rvRooms,progressBar)
                                } catch (e: Exception) {
                                    Log.e(TAG,e.message.toString())
                                    requireContext().unexpectedErrorMessagePrint(e)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG,e.message.toString())
                            requireContext().unexpectedErrorMessagePrint(e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }


    override fun onAdded(documentId: String,documentData: HashMap<String,String>) {
        binding.apply {
            viewModel.apply {
                try {
                    val roomItem = roomDocumentToRoomObj(documentId,documentData)
                    roomPojoList.add(roomItem)
                    roomAdapter.notifyItemInserted(roomAdapter.itemCount)
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    requireContext().unexpectedErrorMessagePrint(e)
                }
            }
        }
    }

    override fun onClick(position: Int) {
    }

    override fun onDeleteClick(position: Int) {

    }
}