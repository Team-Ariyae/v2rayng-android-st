package com.v2ray.ang.ui

import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.databinding.ItemQrcodeBinding
import com.v2ray.ang.databinding.ItemRecyclerFooterBinding
import com.v2ray.ang.databinding.ItemRecyclerMainBinding
import com.v2ray.ang.dto.EConfigType
import com.v2ray.ang.dto.SubscriptionItem
import com.v2ray.ang.extension.toast
import com.v2ray.ang.helper.ItemTouchHelperAdapter
import com.v2ray.ang.helper.ItemTouchHelperViewHolder
import com.v2ray.ang.service.V2RayServiceManager
import com.v2ray.ang.util.AngConfigManager
import com.v2ray.ang.util.MmkvManager
import com.v2ray.ang.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.Collections
import java.util.concurrent.TimeUnit


class MainRecyclerAdapter(val activity: MainActivity) : RecyclerView.Adapter<MainRecyclerAdapter.BaseViewHolder>()
        , ItemTouchHelperAdapter {
    companion object {
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_FOOTER = 2
    }

    private var mActivity: MainActivity = activity
    private val mainStorage by lazy { MMKV.mmkvWithID(MmkvManager.ID_MAIN, MMKV.MULTI_PROCESS_MODE) }
    private val subStorage by lazy { MMKV.mmkvWithID(MmkvManager.ID_SUB, MMKV.MULTI_PROCESS_MODE) }
    private val settingsStorage by lazy { MMKV.mmkvWithID(MmkvManager.ID_SETTING, MMKV.MULTI_PROCESS_MODE) }
    private val share_method: Array<out String> by lazy {
        mActivity.resources.getStringArray(R.array.share_method)
    }
    var isRunning = false
    private var moveBusy = false
    private var ready = false

    private fun getTestDelayMillis(guid: String): Long {
        val testDelayString = MmkvManager.decodeServerAffiliationInfo(guid)?.getTestDelayString()
        return testDelayString?.replace("ms", "")?.trim()?.toLongOrNull() ?: Long.MAX_VALUE
    }

//    fun sortServersBySpeed() {
//        val sortedList = activity.mainViewModel.serversCache.sortedWith(compareBy { getTestDelayMillis(it.guid) })
//        activity.mainViewModel.saveSortCache(sortedList.toMutableList())
//        notifyDataSetChanged()
//    }
//    private val observedItems = mutableSetOf<Int>()

    override fun getItemCount() = mActivity.mainViewModel.serversCache.size + 1

    suspend fun moveTo(from: Int, to: Int) {
        if (moveBusy) {
            delay(300)
            moveTo(from, to)
        } else {
            withContext(Dispatchers.Main) {
                onItemMove(from, to)
            }
        }
    }

    fun sortServersBySpeed() {
        val sortedList = activity.mainViewModel.serversCache.sortedWith(compareBy { getTestDelayMillis(it.guid) })

        CoroutineScope(Dispatchers.Main).launch {
            for (i in sortedList.indices) {
                val oldPosition = activity.mainViewModel.serversCache.indexOfFirst { it.guid == sortedList[i].guid }
                if (oldPosition != i) {
                    moveTo(oldPosition, i)
                }
            }
//            activity.mainViewModel.saveSortCache(sortedList.toMutableList())
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is MainViewHolder) {
            val guid = mActivity.mainViewModel.serversCache[position].guid
            val config = mActivity.mainViewModel.serversCache[position].config

            val outbound = config.getProxyOutbound()
            val aff = MmkvManager.decodeServerAffiliationInfo(guid)

//            // by MRB
//            fun sp() {
//                try{
//                    mActivity.mainViewModel.runtimeUpdateScope.launch {
//
//                        suspend fun move(from: Int, to: Int) {
//                            if (moveBusy) {
//                                delay(300)
//                                move(from, to)
//                            } else {
//                                withContext(Dispatchers.Main) {
//                                    this@MainRecyclerAdapter.onItemMove(from, to)
//                                }
//                            }
//                        }
//
//                        val testDelay1 = aff?.testDelayMillis ?: 0L
//
//                        if(testDelay1 >= 0L){
//                            move(position, this@MainRecyclerAdapter.itemCount - 2)
//                            Log.d("F", "OUT: $position")
//                            return@launch
//                        }else{
//                            Log.d("Fcc", "OUT: $position, 2: $testDelay1")
//                        }
//
////                        if (position >= 1) {
////                            val guid_ = mActivity.mainViewModel.serversCache[position - 1].guid
////                            val aff_ = MmkvManager.decodeServerAffiliationInfo(guid_)
////                            val testDelay2 = aff_?.testDelayMillis ?: 0L
////
////                            if (aff_?.getTestDelayString() != "ms" && !aff_?.getTestDelayString().isNullOrEmpty()) {
////                                if ((aff_?.testDelayMillis ?: 0L) < 0L) {
////                                    move(position, position - 1)
////                                } else {
////                                    if (testDelay1 < testDelay2) {
////                                        move(position, position - 1)
//////                                        mActivity.mainViewModel.runtimeUpdateList.value = guid_
////                                    }
////                                }
////                            } else {
////                                move(position, position - 1)
////                            }
////                        }
//                    }
//                }catch (e: Exception){
//                    mActivity.mainViewModel.clearRuntimelistScope(mActivity)
//                    e.printStackTrace()
//                }
//            }
//
//            sp()

            // چک کنید آیا این آیتم قبلاً مشاهده شده است یا خیر
//            if (!observedItems.contains(position)) {
//                mActivity.mainViewModel.runtimeUpdateList.observe(mActivity) { index ->
////                    if((index ?: "") == guid) {
//                        sp()
////                    }
//                }
//                observedItems.add(position)
//            }

//            mActivity.mainViewModel.runtimeUpdateScope.launch {
//                try{
//                    while (true) {
//                        delay(10000)
//                        sp()
//                    }
//                }catch (e: Exception){
//                    mActivity.mainViewModel.clearRuntimelistScope(mActivity)
//                }
//            }

            holder.itemMainBinding.tvName.text = config.remarks
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            holder.itemMainBinding.tvTestResult.text = aff?.getTestDelayString() ?: ""

            if(aff?.getTestDelayString() != "ms" && !aff?.getTestDelayString().isNullOrEmpty()){
                if ((aff?.testDelayMillis ?: 0L) < 0L) {
                    holder.itemMainBinding.tvTestResult.setTextColor(ContextCompat.getColor(mActivity, R.color.colorPingRed))
                    removeServer(guid, position)
                } else {
                    holder.itemMainBinding.tvTestResult.setTextColor(ContextCompat.getColor(mActivity, R.color.colorPing))
                }
            }
            if (guid == mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER)) {
                holder.itemMainBinding.layoutIndicator.setBackgroundResource(R.color.colorSelected)
            } else {
                holder.itemMainBinding.layoutIndicator.setBackgroundResource(R.color.colorUnselected)
            }
            holder.itemMainBinding.tvSubscription.text = ""
            val json = subStorage?.decodeString(config.subscriptionId)
            if (!json.isNullOrBlank()) {
                val sub = Gson().fromJson(json, SubscriptionItem::class.java)
                holder.itemMainBinding.tvSubscription.text = sub.remarks
            }

            var shareOptions = share_method.asList()
            when (config.configType) {
                EConfigType.CUSTOM -> {
                    holder.itemMainBinding.tvType.text = mActivity.getString(R.string.server_customize_config)
                    shareOptions = shareOptions.takeLast(1)
                }
                EConfigType.VLESS -> {
                    holder.itemMainBinding.tvType.text = config.configType.name
                }
                else -> {
                    holder.itemMainBinding.tvType.text = config.configType.name.lowercase()
                }
            }
            holder.itemMainBinding.tvStatistics.text = "${outbound?.getServerAddress()} : ${outbound?.getServerPort()}"

            holder.itemMainBinding.layoutShare.setOnClickListener {
                AlertDialog.Builder(mActivity).setItems(shareOptions.toTypedArray()) { _, i ->
                    try {
                        when (i) {
                            0 -> {
                                if (config.configType == EConfigType.CUSTOM) {
                                    shareFullContent(guid)
                                } else {
                                    val ivBinding = ItemQrcodeBinding.inflate(LayoutInflater.from(mActivity))
                                    ivBinding.ivQcode.setImageBitmap(AngConfigManager.share2QRCode(guid))
                                    AlertDialog.Builder(mActivity).setView(ivBinding.root).show()
                                }
                            }
                            1 -> {
                                if (AngConfigManager.share2Clipboard(mActivity, guid) == 0) {
                                    mActivity.toast(R.string.toast_success)
                                } else {
                                    mActivity.toast(R.string.toast_failure)
                                }
                            }
                            2 -> shareFullContent(guid)
                            else -> mActivity.toast("else")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.show()
            }

            holder.itemMainBinding.layoutEdit.setOnClickListener {
                val intent = Intent().putExtra("guid", guid)
                        .putExtra("isRunning", isRunning)
                if (config.configType == EConfigType.CUSTOM) {
                    mActivity.startActivity(intent.setClass(mActivity, ServerCustomConfigActivity::class.java))
                } else {
                    mActivity.startActivity(intent.setClass(mActivity, ServerActivity::class.java))
                }
            }
            holder.itemMainBinding.layoutRemove.setOnClickListener {
                if (guid != mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER)) {
                    if (settingsStorage?.decodeBool(AppConfig.PREF_CONFIRM_REMOVE) == true) {
                        AlertDialog.Builder(mActivity).setMessage(R.string.del_config_comfirm)
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                removeServer(guid, position)
                            }
                            .show()
                    } else {
                        removeServer(guid, position)
                    }
                }
            }

            holder.itemMainBinding.infoContainer.setOnClickListener {
                val selected = mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER)
                if (guid != selected) {
                    mainStorage?.encode(MmkvManager.KEY_SELECTED_SERVER, guid)
                    if (!TextUtils.isEmpty(selected)) {
                        notifyItemChanged(mActivity.mainViewModel.getPosition(selected!!))
                    }
                    notifyItemChanged(mActivity.mainViewModel.getPosition(guid))
                    if (isRunning) {
                        mActivity.showCircle()
                        Utils.stopVService(mActivity)
                        Observable.timer(500, TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe {
                                    V2RayServiceManager.startV2Ray(mActivity)
                                    mActivity.hideCircle()
                                }
                    }
                }
            }
        }
        if (holder is FooterViewHolder) {
            //if (activity?.defaultDPreference?.getPrefBoolean(AppConfig.PREF_INAPP_BUY_IS_PREMIUM, false)) {
            if (true) {
                holder.itemFooterBinding.layoutEdit.visibility = View.INVISIBLE
            } else {
                holder.itemFooterBinding.layoutEdit.setOnClickListener {
                    Utils.openUri(mActivity, "${Utils.decode(AppConfig.promotionUrl)}?t=${System.currentTimeMillis()}")
                }
            }
        }
    }

    fun connectToFirst(){
        mActivity.runOnUiThread {
            val selected = mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER)
            val guid = mActivity.mainViewModel.serversCache[0].guid
//            if (guid != selected) {
                mainStorage?.encode(MmkvManager.KEY_SELECTED_SERVER, guid)
                if (!TextUtils.isEmpty(selected)) {
                    notifyItemChanged(mActivity.mainViewModel.getPosition(selected!!))
                }
                notifyItemChanged(mActivity.mainViewModel.getPosition(guid))
                try{
                    mActivity.showCircle()
                    if (isRunning) {
                        Utils.stopVService(mActivity)
                    }
                }finally {
                    Observable.timer(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            V2RayServiceManager.startV2Ray(mActivity)
                            mActivity.hideCircle()
                        }
                }
//            }else{
//                Log.d("S", "NO:(")
//            }
        }
    }

    private fun shareFullContent(guid: String) {
        if (AngConfigManager.shareFullContent2Clipboard(mActivity, guid) == 0) {
            mActivity.toast(R.string.toast_success)
        } else {
            mActivity.toast(R.string.toast_failure)
        }
    }

    private fun removeServer(guid: String,position:Int) {
        mActivity.removeServerSp(guid, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM ->
                MainViewHolder(ItemRecyclerMainBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else ->
                FooterViewHolder(ItemRecyclerFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == mActivity.mainViewModel.serversCache.size) {
            VIEW_TYPE_FOOTER
        } else {
            VIEW_TYPE_ITEM
        }
    }

    open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        fun onItemClear() {
            itemView.setBackgroundColor(0)
        }
    }

    class MainViewHolder(val itemMainBinding: ItemRecyclerMainBinding) :
            BaseViewHolder(itemMainBinding.root), ItemTouchHelperViewHolder

    class FooterViewHolder(val itemFooterBinding: ItemRecyclerFooterBinding) :
            BaseViewHolder(itemFooterBinding.root), ItemTouchHelperViewHolder

    override fun onItemDismiss(position: Int) {
        val guid = mActivity.mainViewModel.serversCache.getOrNull(position)?.guid ?: return
        if (guid != mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER)) {
//            mActivity.alert(R.string.del_config_comfirm) {
//                positiveButton(android.R.string.ok) {
            mActivity.mainViewModel.removeServer(guid)
            notifyItemRemoved(position)
//                }
//                show()
//            }
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        moveBusy = true
        try{
            mActivity.mainViewModel.swapServer(fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
            // position is changed, since position is used by click callbacks, need to update range
            if (toPosition > fromPosition)
                notifyItemRangeChanged(fromPosition, toPosition - fromPosition + 1)
            else
                notifyItemRangeChanged(toPosition, fromPosition - toPosition + 1)
            return true
        }finally {
            moveBusy = false
        }
    }

    override fun onItemMoveCompleted() {
        moveBusy = false
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        ready = true
    }
    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        ready = true
    }
}
