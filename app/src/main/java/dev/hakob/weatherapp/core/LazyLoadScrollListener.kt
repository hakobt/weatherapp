package dev.hakob.weatherapp.core

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class LazyLoadScrollListener(
    private val linearLayoutManager: LinearLayoutManager
) : RecyclerView.OnScrollListener() {

  private var previousTotal = 0 // The total number of items in the dataset after the last load

  private var loading = true // True if we are still waiting for the last set of data to load.

  private val visibleThreshold = 10 // The minimum amount of items to have below your current scroll position before loading more.

  private var firstVisibleItem = 0
  private var visibleItemCount: Int = 0
  private var totalItemCount: Int = 0
  private var currentPage = 0
  private val loadMore = Runnable { onLoadMore(currentPage) }

  fun reset() {
    firstVisibleItem = 0
    visibleItemCount = 0
    totalItemCount = 0
    currentPage = 0
    previousTotal = 0
  }

  override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
    super.onScrolled(recyclerView, dx, dy)
    visibleItemCount = recyclerView.childCount
    totalItemCount = linearLayoutManager.itemCount
    firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()
    if (loading) {
      if (totalItemCount > previousTotal || totalItemCount == 0) {
        loading = false
        previousTotal = totalItemCount
      }
    }
    // End has been reached
    if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {
      currentPage++
      recyclerView.post(loadMore)
      loading = true
    }
  }

  abstract fun onLoadMore(currentPage: Int)
}

inline fun RecyclerView.addPageListener(crossinline onLoadMore: (currentPage: Int) -> Unit): LazyLoadScrollListener {
  val listener = object : LazyLoadScrollListener(layoutManager as LinearLayoutManager) {
    override fun onLoadMore(currentPage: Int) {
      onLoadMore(currentPage)
    }
  }
  addOnScrollListener(listener)
  return listener
}