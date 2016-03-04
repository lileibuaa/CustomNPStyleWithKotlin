package li.lei.npStyleWithKotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvHello.text = "from kotlin"
        btShowToast.setOnClickListener({ view ->
            Toast.makeText(this@MainActivity, "hello ${view.id}", Toast.LENGTH_SHORT).show()
        })
    }
}
