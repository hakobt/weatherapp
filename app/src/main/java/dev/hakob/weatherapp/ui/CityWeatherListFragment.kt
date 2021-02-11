package dev.hakob.weatherapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.hakob.weatherapp.R
import dev.hakob.weatherapp.core.EventObserver
import dev.hakob.weatherapp.core.Resource
import dev.hakob.weatherapp.core.SingleEvent
import dev.hakob.weatherapp.core.addPageListener
import dev.hakob.weatherapp.data.entity.CityWeather
import dev.hakob.weatherapp.databinding.LayoutWeatherListBinding
import dev.hakob.weatherapp.network.ConnectivityManager
import timber.log.Timber

@AndroidEntryPoint
class CityWeatherListFragment : Fragment(R.layout.layout_weather_list) {

    private val viewModel by viewModels<CityWeatherListViewModel>()

    private lateinit var adapter: CityWeatherListAdapter
    private lateinit var addCityDialog: AlertDialog
    private lateinit var binding: LayoutWeatherListBinding

    private val noInternetSnackbar by lazy {
        Snackbar.make(
            binding.root,
            "Enable internet and click the plus button to add a location for weather",
            Snackbar.LENGTH_INDEFINITE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createAddCityDialog(container)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun createAddCityDialog(parent: ViewGroup?) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        val dialogView = LayoutInflater.from(requireContext()).inflate(
            R.layout.dialog_add_city,
            parent,
            false
        )
        alertDialogBuilder.setView(dialogView)
        val okButton = dialogView.findViewById<MaterialButton>(R.id.positiveButton)
        val closeButton = dialogView.findViewById<MaterialButton>(R.id.negativeButton)

        val editText = dialogView.findViewById<EditText>(R.id.addCityEditText)

        okButton.setOnClickListener {
            if (editText.text.isNotEmpty()) {
                viewModel.addCity(editText.text.toString())
                addCityDialog.dismiss()
            }
        }

        closeButton.setOnClickListener {
            addCityDialog.dismiss()
        }

        addCityDialog = alertDialogBuilder.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = LayoutWeatherListBinding.bind(view)

        binding.addCityButton.setOnClickListener {
            addCityDialog.show()
        }

        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.END or ItemTouchHelper.START
        ) {
            var dragFromPosition = -1
            var dragToPosition = -1
            var item: CityWeather? = null

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if (dragFromPosition == -1) {
                    dragFromPosition = viewHolder.adapterPosition
                    item = adapter.currentList[dragFromPosition]
                }
                dragToPosition = target.adapterPosition
                adapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            private fun definitelyMoved(
                item: CityWeather,
                fromPosition: Int,
                toPosition: Int
            ) {
                viewModel.onItemMoved(item, fromPosition, toPosition)
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_IDLE -> {
                        if (dragFromPosition != -1 && dragToPosition != -1 && dragFromPosition != dragToPosition) {
                            // Item successfully dragged
                            definitelyMoved(item!!, dragFromPosition, dragToPosition)
                            // Reset drag positions
                            dragFromPosition = -1
                            dragToPosition = -1
                            item = null
                        }
                    }
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                try {
                    val item = adapter.currentList[position]
                    viewModel.removeCity(item)
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }
            }
        })
        adapter = CityWeatherListAdapter(
            clickHandler = { },
            longClickHandler = { position ->
                val viewHolder = binding.listView.findViewHolderForAdapterPosition(position)
                    ?: return@CityWeatherListAdapter
                itemTouchHelper.startDrag(viewHolder)
            }
        )

        binding.listView.adapter = adapter
        itemTouchHelper.attachToRecyclerView(binding.listView)
        binding.listView.layoutManager = layoutManager
        binding.listView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                RecyclerView.VERTICAL
            )
        )

        binding.listView.addPageListener {
            viewModel.onEndReached()
        }

        viewModel.cityList.observe(viewLifecycleOwner, ::bindToView)
        viewModel.events.observe(viewLifecycleOwner, EventObserver(::showEvent))
    }

    private fun showEvent(event: CityWeatherListViewModel.Event) {
        when(event) {
            is CityWeatherListViewModel.Event.AddCity -> {
                if (!event.success) {
                    Snackbar.make(binding.root, "Adding ${event.name} failed", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun bindToView(resource: Resource<List<CityWeather>>) {
        when (resource) {
            is Resource.Error -> {
                // can show error to user
            }
            is Resource.Loading -> {
                // show loading to user
            }
            is Resource.Success -> {
                if (resource.data.isNullOrEmpty()
                    && resource.networkState == ConnectivityManager.NetworkState.DISCONNECTED
                ) {
                    showHint()
                    return
                }
                hideHint()
                adapter.submitList(resource.data)
            }
        }
    }

    private fun hideHint() {
        noInternetSnackbar.dismiss()
    }

    private fun showHint() {
        noInternetSnackbar.show()
    }
}
