package com.program.mvvm.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.program.mvvm.model.DogBreed
import com.program.mvvm.model.DogDatabase
import com.program.mvvm.util.BaseViewModel
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : BaseViewModel(application) {

    val dogLiveData =  MutableLiveData<DogBreed>()

      fun fetch(uuid: Int) {
          launch {
              val dog = DogDatabase(getApplication()).dogDao().getDog(uuid)
              dogLiveData.value = dog
          }
      }
}
