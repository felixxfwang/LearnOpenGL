package com.tencent.nijigen.learnopengl

import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.tencent.nijigen.learnopengl.base.BaseRenderer
import com.tencent.nijigen.learnopengl.render.L8_1_FilterRenderer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity(), GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var triangle: Square02? = null
    //投影矩阵
    private val mProjectionMatrix = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(2)
        val renderer = L8_1_FilterRenderer(this)
        glSurfaceView.setRenderer(this)
        glSurfaceView.setOnClickListener {
            renderer.onClick()
        }
        setContentView(glSurfaceView)
    }

    private var vertexArrayBuffer: FloatBuffer? = null
    private var texArrayBuffer: FloatBuffer? = null

    private var program = -1
    private var aPositionLocation = -1
    private var aColorLocation = -1
    private var uMvpMatrixLocation = -1
    private var textureId = -1

    private var filterRenderer: BaseRenderer? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.i(TAG, "onSurfaceCreated")
        // 设置个红色背景
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)

        triangle = Square02(BitmapFactory.decodeResource(resources, R.drawable.test))

        val vertices = floatArrayOf(
            -0.5f, 0.5f, 0f,
            0.5f, 0.5f, 0f,
            0.5f, -0.5f, 0f,
            -0.5f, -0.5f, 0f
        )

        val colorData = floatArrayOf(
            // 一个顶点对应1个三维向量：r、g、b
            1f, 0.5f, 0.5f,
            1f, 0f, 1f,
            0f, 1f, 1f,
            1f, 1f, 0f
        )

        val qbb = ByteBuffer
            .allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
        vertexArrayBuffer = qbb.asFloatBuffer()
        vertexArrayBuffer?.put(vertices)
        vertexArrayBuffer?.position(0)

//        val texVertices = floatArrayOf(
//            0f, 0f,
//            1f, 0f,
//            1f, 1f,
//            0f, 1f
//        )
//        val texByteBuffer = ByteBuffer
//            .allocateDirect(texVertices.size * 4)
//            .order(ByteOrder.nativeOrder())
//        texArrayBuffer = texByteBuffer.asFloatBuffer()
//        texArrayBuffer?.put(texVertices)
//        texArrayBuffer?.position(0)


        val texByteBuffer = ByteBuffer
            .allocateDirect(colorData.size * 4)
            .order(ByteOrder.nativeOrder())
        texArrayBuffer = texByteBuffer.asFloatBuffer()
        texArrayBuffer?.put(colorData)
        texArrayBuffer?.position(0)

        program = GLUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        aPositionLocation = GLES20.glGetAttribLocation(program, "a_Position")
        aColorLocation = GLES20.glGetAttribLocation(program, "a_Color")
//        uMvpMatrixLocation = GLES20.glGetUniformLocation(program, "u_MvpMatrix")

//        val textures = IntArray(1) //生成纹理id
//        GLES20.glGenTextures(  //创建纹理对象
//            1, //产生纹理id的数量
//            textures, //纹理id的数组
//            0  //偏移量
//        )
//        textureId = textures[0]
//
//        //绑定纹理id，将对象绑定到环境的纹理单元
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
//
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//            GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())//设置MIN 采样方式
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//            GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat()) //设置MAG采样方式
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//            GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())//设置S轴拉伸方式
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//            GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())//设置T轴拉伸方式
//
//        GLUtils.texImage2D( //实际加载纹理进显存
//            GLES20.GL_TEXTURE_2D, //纹理类型
//            0, //纹理的层次，0表示基本图像层，可以理解为直接贴图
//            BitmapFactory.decodeResource(resources, R.drawable.test), //纹理图像
//            0 //纹理边框尺寸
//        )
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.i(TAG, "onSurfaceChanged")
        // 边长比(>=1)，非宽高比
        val aspectRatio = if (width > height)
            width.toFloat() / height
        else
            height.toFloat() / width

        // 1. 矩阵数组
        // 2. 结果矩阵起始的偏移量
        // 3. left：x的最小值
        // 4. right：x的最大值
        // 5. bottom：y的最小值
        // 6. top：y的最大值
        // 7. near：z的最小值
        // 8. far：z的最大值
        if (width > height) {
            // 横屏
            Matrix.orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio,
                -1f, 1f, -1f, 1f)
        } else {
            // 竖屏or正方形
            Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f,
                -aspectRatio, aspectRatio, -1f, 1f)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        // Redraw background color 重绘背景
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//        Log.i(TAG, "onDrawFrame")
//        triangle?.setMvpMatrix(mProjectionMatrix)
//        triangle?.draw()

        drawRect()
    }


//    private val VERTEX_SHADER = "" +
//            "uniform mat4 u_MvpMatrix;\n" +
//            "attribute vec4 a_Position;\n" +
//            "attribute vec2 a_TexCoord;\n" +
//            "varying vec2 v_TexCoord;\n" +
//            "void main()\n" +
//            "{\n" +
//            "    v_TexCoord = a_TexCoord;\n" +
//            "    gl_Position = u_MvpMatrix * a_Position;\n" +
//            "}"

//    private val FRAGMENT_SHADER = "" +
//            "varying vec2 v_TexCoord;\n" +
//            "uniform sampler2D u_Texture;\n" +
//            "void main()\n" +
//            "{\n" +
//            "    gl_FragColor = texture2D(u_Texture, v_TexCoord);\n" +
//            "}"

    private val VERTEX_SHADER = "" +
        "attribute vec4 a_Position;\n" +
        "attribute vec4 a_Color;\n" +
        "varying vec4 v_Color;\n" +
        "void main()\n" +
        "{\n" +
        "    v_Color = a_Color;\n" +
        "    gl_Position = a_Position;\n" +
        "}"
    private val FRAGMENT_SHADER = "" +
            "varying vec4 v_Color;\n" +
            "void main()\n" +
            "{\n" +
            "    gl_FragColor = v_Color;\n" +
            "}"

    private fun drawRect() {
        GLES20.glUseProgram(program)

        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT,
            false, 0, vertexArrayBuffer)
        GLES20.glVertexAttribPointer(aColorLocation, 3, GLES20.GL_FLOAT,
            false, 0, texArrayBuffer)

        GLES20.glEnableVertexAttribArray(aPositionLocation)
        GLES20.glEnableVertexAttribArray(aColorLocation)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
    }

}
