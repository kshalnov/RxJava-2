package ru.gb.course1.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.gb.course1.myapplication.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var disposableLeft: Disposable? = null
    private var disposableRight: Disposable? = null
    private var disposableConnect: Disposable? = null

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        binding.connectButton.setOnClickListener {
//            disposableConnect = observable.connect()
//            disposeOnDestroy(disposableConnect)
//        }

        binding.disposeButton.setOnClickListener {
            disposableConnect?.dispose()
        }

        binding.headerTextView.text = "Observable.create<String> { ... }.publish()"

        binding.leftSubscribeButton.setOnClickListener { _ ->
            disposableLeft = observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { data -> onNextLeft(data) },
                    onError = { throwable -> onErrorLeft(throwable) },
                    onComplete = { onCompleteLeft() }
                )
            compositeDisposable.add(disposableLeft)
        }

        binding.rightSubscribeButton.setOnClickListener { _ ->
            disposableRight = observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { data -> onNextRight(data) },
                    onError = { throwable -> onErrorRight(throwable) },
                    onComplete = { onCompleteRight() }
                )
            disposeOnDestroy(disposableRight)
        }

        binding.leftDisposeButton.setOnClickListener {
            disposableLeft?.dispose()
        }
        binding.rightDisposeButton.setOnClickListener {
            disposableRight?.dispose()
        }
    }

    private val observable = Observable.create<String> { emitter ->
        try {
            var counter = 0
            while (!Thread.currentThread().isInterrupted) {
                try {
                    emitter.onNext("${counter++} // ${UUID.randomUUID()}")
                    throw RuntimeException("Ой, ошибочка!")
                } catch (ex: Exception) {
                    // да и пофигу, работаем дальше
                }
                Thread.sleep(1000)
            }
        } catch (ie: InterruptedException) {
            runOnUiThread {
                Toast.makeText(this, "InterruptedException", Toast.LENGTH_SHORT).show()
            }
        } finally {
            emitter.onComplete()
        }
    }.subscribeOn(Schedulers.computation()).replay().refCount()

    private fun onCompleteLeft() {
        onNextLeft("COMPLETED")
    }

    private fun onErrorLeft(it: Throwable) {
        it.message?.let {
            onNextLeft(it)
        }
    }

    private fun onNextLeft(text: String) {
        val oldText = binding.leftResultTextView.text.toString()
        binding.leftResultTextView.text = oldText + "\n" + text
    }

    private fun onCompleteRight() {
        onNextRight("COMPLETED")
    }

    private fun onErrorRight(it: Throwable) {
        it.message?.let {
            onNextRight(it)
        }
    }

    private fun onNextRight(text: String) {
        val oldText = binding.rightResultTextView.text.toString()
        binding.rightResultTextView.text = oldText + "\n" + text
    }

    protected fun disposeOnDestroy(disposable: Disposable?) {
        if (disposable == null) return
        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}