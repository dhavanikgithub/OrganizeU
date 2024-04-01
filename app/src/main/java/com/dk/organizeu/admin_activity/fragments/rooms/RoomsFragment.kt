package com.dk.organizeu.admin_activity.fragments.rooms

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.adapter.RoomAdapter
import com.dk.organizeu.admin_activity.data_class.Room
import com.dk.organizeu.admin_activity.dialog_box.AddAcademicDialog
import com.dk.organizeu.admin_activity.dialog_box.AddRoomDialog
import com.dk.organizeu.admin_activity.listener.AcademicAddListener
import com.dk.organizeu.admin_activity.listener.OnAcademicItemClickListener
import com.dk.organizeu.admin_activity.listener.OnRoomItemClickListener
import com.dk.organizeu.admin_activity.listener.RoomAddListener
import com.dk.organizeu.databinding.FragmentFacultyBinding
import com.dk.organizeu.databinding.FragmentRoomsBinding
import com.dk.organizeu.utils.CustomProgressDialog
import com.google.firebase.firestore.FirebaseFirestore

class RoomsFragment : Fragment(), RoomAddListener, OnRoomItemClickListener {

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
                roomList.clear()
                roomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                db.collection("room")
                    .get()
                    .addOnSuccessListener {documents->
                        for(document in documents)
                        {
                            val roomItem = Room(document.id,document.get("location").toString(),document.get("type").toString())
                            roomList.add(roomItem)
                        }
                        roomAdapter = RoomAdapter(roomList,this@RoomsFragment)
                        roomsRecyclerView.adapter = roomAdapter
                    }
            }
        }
    }

    override fun onRoomAdded(roomData: HashMap<String,String>,roomDocumentId:String) {
        binding.apply {
            viewModel.apply {
                val roomItem = Room(roomDocumentId,
                    roomData["location"].toString(), roomData["type"].toString())
                roomList.add(roomItem)
                roomAdapter.notifyItemInserted(roomAdapter.itemCount)
            }
        }

    }

    override fun onItemClick(position: Int) {
    }
}