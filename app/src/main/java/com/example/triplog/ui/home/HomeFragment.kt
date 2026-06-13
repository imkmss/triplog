package com.example.triplog.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.triplog.R
import com.example.triplog.data.DBHelper
import com.example.triplog.databinding.FragmentHomeBinding
import com.example.triplog.ui.edit.AddEditActivity
import com.example.triplog.ui.DetailActivity


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: TripAdapter
    private var sortOrder = "DESC"

    private val addEditLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DBHelper(requireContext())
        adapter = TripAdapter(mutableListOf())

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // 옵션 메뉴 등록
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_sort -> {
                        sortOrder = if (sortOrder == "DESC") "ASC" else "DESC"
                        loadData()
                        true
                    }
                    R.id.menu_delete_all -> {
                        showDeleteAllDialog()
                        true
                    }
                    R.id.menu_info -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("앱 정보")
                            .setMessage("여행 기록 앱 Triplog\nVersion 1.0")
                            .setPositiveButton("확인", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // FAB 추가 버튼
        binding.fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), AddEditActivity::class.java)
            addEditLauncher.launch(intent)
        }

        // 아이템 클릭 시 수정 화면으로 이동
        adapter.onItemClick = { record ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("record_no", record.no)
            startActivity(intent)
        }

        // 아이템 길게 누르기 - 컨텍스트 메뉴
        adapter.onItemLongClick = { record ->
            AlertDialog.Builder(requireContext())
                .setTitle("기록 관리")
                .setItems(arrayOf("수정", "삭제")) { _, which ->
                    when (which) {
                        0 -> {
                            val intent = Intent(requireContext(), AddEditActivity::class.java)
                            intent.putExtra("record_no", record.no)
                            addEditLauncher.launch(intent)
                        }
                        1 -> showDeleteDialog(record.no)
                    }
                }
                .show()
        }

        loadData()
    }

    private fun showDeleteDialog(no: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("삭제 확인")
            .setMessage("이 기록을 삭제할까요?")
            .setPositiveButton("삭제") { _, _ ->
                dbHelper.delete(no)
                loadData()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showDeleteAllDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("전체 삭제 확인")
            .setMessage("모든 여행 기록을 삭제할까요?")
            .setPositiveButton("삭제") { _, _ ->
                dbHelper.deleteAll()
                loadData()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    fun loadData() {
        val list = dbHelper.getAll(sortOrder)
        adapter.updateList(list.toMutableList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}