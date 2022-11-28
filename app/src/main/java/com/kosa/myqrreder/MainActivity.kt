package com.kosa.myqrreder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.kosa.myqrreder.databinding.ActivityMainBinding
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>// 병렬 프로그래밍에서 태스크가 제대로 끝났는지 확인할 때 쓴다고함


    private val PERMISSIONS_REQUEST_CODE = 1 //REQUEST 값을 받기위한 용도 0이상이면됨
    private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

    private var isDetected = false

    override fun onResume() {
        super.onResume()
        isDetected = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        val view = binding.root //바인딩 객체의root뷰 참조!
        setContentView(view)// 생성한 뷰 설정
//        setContentView(R.layout.activity_main)

        if (!hasPermissions(this)) {
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        } else {
            startCamera()//카메라 시작 ~
        }
    }

    fun getIamgeAnalysis() : ImageAnalysis{
        val cameraExecutor : ExecutorService = Executors.newSingleThreadExecutor()
        val imageAnalysis = ImageAnalysis.Builder().build()

        //우리가 따로 정의한 인터페이스를 분석기로 지정한뒤에 만든 객체를 반환한다!!!
        imageAnalysis.setAnalyzer(cameraExecutor, QRCodeAnalyzer(object : OnDetectListener{
            override fun onDetect(msg: String) {
                if(!isDetected){
                    isDetected = true
                    val intent = Intent(this@MainActivity, ResultActivity::class.java)
                    intent.putExtra("msg", msg)
                    startActivity(intent)
//                    Toast.makeText(this@MainActivity, "${msg}",
//                        Toast.LENGTH_LONG).show()
                }

            }
        }))
        return imageAnalysis
    }


    fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {// all은 배열안에 값들이 모두 참이면 참을 뱉고 하나라도 아니면 false를 뱉는 그런 함수인가봐
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                Toast.makeText(this@MainActivity, "권한 요청이 승인되었습니다.", Toast.LENGTH_LONG).show()
                startCamera()
            } else {
                Toast.makeText(this@MainActivity, "권한 요청이 거부되었습니다.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    }

    //미리보기와 이미지 분석 시작
    fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {//Runnable이 뭐지?
            val cameraProvider = cameraProviderFuture.get()

            val preview = getPreview()
            val imageAnalysis = getIamgeAnalysis()//이미지 분석을 위한 객체를 만들고 이 인스턴스를 넘겨주면 분석한뒤 onDetect 함수를 실행할꺼야
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA//후면 카메라를 선택 ~

            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)// 미리보기+ 이미지 분석까지 기능 선택
        }, ContextCompat.getMainExecutor(this))
    }

    //미리보기 객체를 반환(카메라의 미리보기 겠지)
    fun getPreview(): Preview {
        val preview: Preview = Preview.Builder().build()//Preview 객체 생성
        preview.setSurfaceProvider(binding.barcodePreview.surfaceProvider)// 픽셀을 만들어서 뷰를 만들어주는 provider 인가봐
        return preview
    }
}