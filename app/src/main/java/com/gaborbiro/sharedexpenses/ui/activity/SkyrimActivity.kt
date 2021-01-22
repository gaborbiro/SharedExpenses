package com.gaborbiro.sharedexpenses.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.util.Log
import android.view.View
import com.gaborbiro.sharedexpenses.R
import com.gaborbiro.sharedexpenses.ui.adapter.SkyrimAdapter
import com.gaborbiro.sharedexpenses.ui.model.Candidate
import com.gaborbiro.sharedexpenses.ui.model.Permutation
import com.gaborbiro.sharedexpenses.util.Lce
import com.gaborbiro.sharedexpenses.util.TextWatcherAdapter
import com.gaborbiro.sharedexpenses.util.hide
import com.gaborbiro.sharedexpenses.util.notNull
import com.gaborbiro.sharedexpenses.util.show
import com.google.common.collect.ImmutableList
import com.googlecode.tesseract.android.TessBaseAPI
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.activity_skyrim.*
import kotlinx.android.synthetic.main.content_skyrim.*


class SkyrimActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, SkyrimActivity::class.java))
        }

//        const val REQUEST_GALLERY = 1
    }

    private val adapter: SkyrimAdapter = SkyrimAdapter()

    private var subscription: Disposable? = null
    private var currentThings: List<String>? = null
    private var currentSize: Int? = null
    private var lastProgress: Int? = null

    private val tess: TessBaseAPI = TessBaseAPI()
    private var photo: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skyrim)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        things_input.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(editable: Editable?) {
                onInputUpdated()
            }
        })
        size_input.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(editable: Editable?) {
                onInputUpdated()
            }
        })
        list.adapter = adapter
    }

    private fun onInputUpdated() {
        val things = things_input.text.toString()
        val size = size_input.text.toString()

        if (things.isNotEmpty() && size.isNotEmpty()) {
            val thingsArray = things.split("[,;\\s]+".toRegex()).map { it.toLowerCase().capitalize().trim() }.filter { it.isNotBlank() }
            val sizeInt = size.toInt()

            if (currentThings?.equals(thingsArray) == false || currentSize != sizeInt) {
                subscription?.dispose()
                currentThings = thingsArray
                currentSize = sizeInt
                subscription = generate(thingsArray, sizeInt)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (it.loading) {
                                progress.show()
                                list.hide()
                                progress.text = "${it.progress}%"
                            } else {
                                progress.hide()
                                list.show()
                                adapter.data = it.content?.toList()
                            }
                        }
            }
        }
    }

    private fun generate(things: List<String>, size: Int): Observable<Lce<Array<Permutation>>> {
        return Observable.create<Lce<Array<Permutation>>> { emitter ->
            emitter.onNext(Lce.loading(0))
            val collector = PublishSubject.create<Candidate>()
            val result = mutableListOf<Permutation>()
            Pair(things, size).notNull { things, size ->
                val target = Math.pow(things.size.toDouble(), size.toDouble())
                collector
                        .doOnComplete {
                            result.sortBy { it.duplicateCount }
                            emitter.onNext(Lce.content(result.toTypedArray()))
                            subscription = null
                        }
                        .subscribe {
                            val data: ImmutableList<String> = ImmutableList.copyOf(it.data.map { things[it] })
                            result.add(Permutation(things = data, duplicateCount = it.longestRepeat()))

                            ((result.size / target) * 100).toInt().let {
                                if (it != lastProgress) {
                                    lastProgress = it
                                    emitter.onNext(Lce.loading(it))
                                }
                            }
                        }
                permutate(Candidate(size), things.size, collector)
                collector.onComplete()
            }
        }
    }

    private fun permutate(candidate: Candidate, count: Int, emitter: Subject<Candidate>, index: Int = 0) {
        while (candidate.data[index] < count - 1) {
            candidate.inc(index)
            if (index < candidate.data.size - 1) {
                permutate(candidate, count, emitter, index + 1)
            } else {
                emitter.onNext(candidate)
            }
        }
        candidate.clear(index)
    }

    fun onSelectFileClicked(view: View) {
//        val photoPickerIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        photoPickerIntent.type = "image/*"
//        startActivityForResult(photoPickerIntent, REQUEST_GALLERY)
        loadImage("/storage/emulated/0/Pictures/Screenshots/Screenshot_20180218-084248~2.png")
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
//        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
//            val selectedImage = data.data
//            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
//            println("image:" + selectedImage)
//            val cursor = contentResolver.query(selectedImage,
//                    filePathColumn, null, null, null)
//            cursor.moveToFirst()
//            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
//            val picturePath = cursor.getString(columnIndex)
//            cursor.close()
//            loadImage(picturePath)
//        }
//    }

    fun loadImage(picturePath: String) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(picturePath, options)
        val originalHeight = options.outHeight
        val originalWidth = options.outWidth
        val requiredSize = window.decorView.width
        var sampleSize = Math.max(originalWidth / requiredSize, originalHeight / requiredSize)
        if (sampleSize < 1) {
            sampleSize = 1
        }
        options.inSampleSize = sampleSize
        options.inPurgeable = true
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inJustDecodeBounds = false
        photo = BitmapFactory.decodeFile(picturePath, options)
        this.image.setImageBitmap(photo)

        Single.fromCallable<String> {
            tess.init("/sdcard/CanIBuyThat/", "eng")
            tess.setImage(photo)
            val result = tess.utF8Text
            tess.clear()
            result
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    things_input.setText(it)
                }, {
                    Log.e("SkyrimActivity", "Error processing image", it)
                })
    }
}