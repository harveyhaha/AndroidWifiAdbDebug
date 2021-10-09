package com.harveyhaha.androidwifidebug

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.harveyhaha.androidwifidebug.databinding.FragmentMainBinding
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var status = false
    private var TAG = MainFragment::class.java.simpleName
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startBtn.setOnClickListener {
            if (!status) {
                binding.startBtn.text = resources.getString(R.string.wifi_adb_setting_stop);
                startAdbWifiDebug();
                status = true;
            } else {
                binding.startBtn.text = resources.getString(R.string.wifi_adb_setting_start);
                stopAdbWifiDebug();
                status = false;
            }
        }
        binding.portEt.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    Log.i(TAG, "onKey ACTION_DOWN 确定键")
                    SoftInputUtil.showKeyboard(v)
                    return@setOnKeyListener true
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    binding.startBtn.requestFocus()
                    return@setOnKeyListener true
                }
            } else if (event.action == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    Log.i(TAG, "onKey ACTION_UP 确定键")
                    return@setOnKeyListener true
                }
            }
            return@setOnKeyListener false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //停止
    private fun stopAdbWifiDebug() {
        if (status) {
            command("stop adbd")
            binding.tipsTv.text = ""
        }
    }

    //启动
    private fun startAdbWifiDebug() {
        if (!status) {
            val ip = getIP()
            if (ip == "") {
                binding.tipsTv.text = getString(R.string.error_wifi_empty)
                return
            } else if (binding.portEt.text.isEmpty()) {
                binding.tipsTv.text = getString(R.string.error_port_empty)
                return
            }
            command("setprop service.adb.tcp.port " + binding.portEt.text.toString() + ";stop adbd;start adbd")
            binding.tipsTv.text = getString(R.string.wifi_tips_success, ip, binding.portEt.text.toString())
        }
    }

    //执行命令
    private fun command(cmd: String) {
        try {
            val p = Runtime.getRuntime().exec("su")
            val os: OutputStream = p.outputStream
            val osw = OutputStreamWriter(os)
            val bw = BufferedWriter(osw)
            bw.write(
                """
                $cmd
                
                """.trimIndent()
            )
            bw.write("exit \n")
            bw.close()
            osw.close()
            os.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //获取ip
    private fun getIP(): String {
        val wm = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wm.isWifiEnabled) {
            wm.isWifiEnabled = true
        }
        val wi = wm.connectionInfo
        val ip = wi.ipAddress
        return if (ip == 0) {
            ""
        } else intToIp(ip)
    }

    //int转ip
    private fun intToIp(i: Int): String {
        return (i and 0xFF).toString() + "." + (i shr 8 and 0xFF) + "." + (i shr 16 and 0xFF) + "." + (i shr 24 and 0xFF)
    }
}