package com.example.train_ticket_booking_system.ui.train

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.train_ticket_booking_system.TTBSApplication
import com.example.train_ticket_booking_system.data.entity.Train
import com.example.train_ticket_booking_system.data.repository.TrainWithStops
import com.example.train_ticket_booking_system.data.repository.OrderRepository
import com.example.train_ticket_booking_system.data.repository.TrainRepository
import com.example.train_ticket_booking_system.data.repository.StationRepository
import android.util.Log
import com.example.train_ticket_booking_system.data.entity.SeatType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TrainListState(
    val loading: Boolean = false,
    val trains: List<TrainWithStops> = emptyList(),
    val fromStationName: String = "",
    val toStationName: String = "",
    val date: String = "",
    val error: String? = null
)

class TrainListViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as TTBSApplication).database
    private val trainRepo = TrainRepository(db.trainDao())
    private val stationRepo = StationRepository(db.stationDao())

    private val _state = MutableStateFlow(TrainListState())
    val state: StateFlow<TrainListState> = _state

    fun search(fromId: Long, toId: Long, date: String) {
        Log.d("TTBS_TRAIN", "TrainListViewModel.search: from=$fromId to=$toId date=$date")
        _state.value = _state.value.copy(loading = true, date = date, error = null)
        viewModelScope.launch {
            try {
                val fromStation = stationRepo.getById(fromId)
                val toStation = stationRepo.getById(toId)
                val trains = trainRepo.search(fromId, toId, date)
                _state.value = _state.value.copy(
                    loading = false,
                    trains = trains,
                    fromStationName = fromStation?.name ?: "",
                    toStationName = toStation?.name ?: ""
                )
            } catch (e: Exception) {
                Log.e("TTBS_TRAIN", "TrainListViewModel.search error", e)
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }
}
