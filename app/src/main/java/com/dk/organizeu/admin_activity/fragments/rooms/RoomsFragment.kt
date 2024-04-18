package com.dk.organizeu.admin_activity.fragments.rooms

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.adapter.RoomAdapter
import com.dk.organizeu.admin_activity.dialog.AddRoomDialog
import com.dk.organizeu.databinding.FragmentRoomsBinding
import com.dk.organizeu.listener.AddDocumentListener
import com.dk.organizeu.listener.OnItemClickListener
import com.dk.organizeu.repository.RoomRepository
import com.dk.organizeu.repository.RoomRepository.Companion.roomDocumentToRoomObj
import com.dk.organizeu.utils.CustomProgressDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class RoomsFragment : Fragment(), AddDocumentListener, OnItemClickListener {

    companion object {
        fun newInstance() = RoomsFragment()
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
               initRecyclerView()

                btnAddRoom.setOnClickListener {
                    val dialogFragment = AddRoomDialog()
                    dialogFragment.show(childFragmentManager, "customDialog")
                }
            }
        }
    }

    private fun initRecyclerView()
    {
        binding.apply {
            viewModel.apply {
                showProgressBar()
                MainScope().launch(Dispatchers.IO){
                    roomPojoList.clear()
                    val documents = RoomRepository.getAllRoomDocument()
                    for(document in documents)
                    {
                        val roomItem = roomDocumentToRoomObj(document)
                        roomPojoList.add(roomItem)
                    }
                    withContext(Dispatchers.Main)
                    {
                        roomAdapter = RoomAdapter(roomPojoList,this@RoomsFragment)
                        rvRooms.layoutManager = LinearLayoutManager(requireContext())
                        rvRooms.adapter = roomAdapter
                        delay(500)
                        hideProgressBar()
                    }
                }
            }
        }
    }

    fun showProgressBar()
    {
        binding.rvRooms.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar()
    {
        binding.progressBar.visibility = View.GONE
        binding.rvRooms.visibility = View.VISIBLE
    }

    override fun onAdded(documentId: String,documentData: HashMap<String,String>) {
        binding.apply {
            viewModel.apply {
                val roomItem = roomDocumentToRoomObj(documentId,documentData)
                roomPojoList.add(roomItem)
                roomAdapter.notifyItemInserted(roomAdapter.itemCount)
            }
        }

    }

    override fun onClick(position: Int) {
    }

    override fun onDeleteClick(position: Int) {

    }
}