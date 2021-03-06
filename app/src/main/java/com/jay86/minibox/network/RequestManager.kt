package com.jay86.minibox.network

import com.jay86.minibox.BuildConfig
import com.jay86.minibox.bean.ApiWrapper
import com.jay86.minibox.bean.User
import com.jay86.minibox.config.BASE_URL
import com.jay86.minibox.network.observer.BaseObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 网络请求框架
 * Created by Jay on 2017/10/10.
 */
object RequestManager {
    const val REQUEST_SUCCESSFUL = "200"
    private const val DEFAULT_TIME_OUT = 30

    private val apiService: ApiService

    init {
        val client = configureOkHttp(OkHttpClient.Builder())
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    private fun configureOkHttp(builder: OkHttpClient.Builder): OkHttpClient {
        builder.connectTimeout(DEFAULT_TIME_OUT.toLong(), TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logging)
        }
        return builder.build()
    }

    fun login(phone: String, password: String, observer: BaseObserver<User>) {
        apiService.login(phone, password)
                .map { it.nextOrError() }
                .subscriber(observer)
    }

    fun register(nickname: String, phone: String, password: String, gender: String, observer: BaseObserver<User>) {
        apiService.register(nickname, phone, password, gender)
                .map { it.nextOrError() }
                .subscriber(observer)
    }

    fun sendSms(phoneNumber: String, observer: BaseObserver<String>) {
        apiService.sendSms(phoneNumber)
                .map { it.nextOrError() }
                .subscribe(observer)
    }

    private fun <T> ApiWrapper<T>.nextOrError() =
            if (status != REQUEST_SUCCESSFUL) throw ApiException(status, message) else data

    private fun <T> Observable<T>.subscriber(observer: Observer<T>) {
        subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }
}