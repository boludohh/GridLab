package com.gridlab.app

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Simple provider used to verify constructor injection works end-to-end.
 */
class MessageProvider @Inject constructor() {
    val message: String = "Hilt + KSP working"
}

/**
 * Trivial ViewModel that receives an injected dependency.
 * Used only to confirm the full Hilt → KSP → hiltViewModel() chain on device.
 */
@HiltViewModel
class DemoViewModel @Inject constructor(
    messageProvider: MessageProvider,
) : ViewModel() {
    val message: String = messageProvider.message
}
