package com.lucilab.surveynext.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bumped by repositories after any write, so screens further back in the stack
 * (home, lists, profile) reload without passing results between destinations.
 * Collecting it also triggers the first load, since a StateFlow replays its value.
 */
@Singleton
class DataChangeNotifier @Inject constructor() {
    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version.asStateFlow()

    fun notifyChanged() = _version.update { it + 1 }
}
