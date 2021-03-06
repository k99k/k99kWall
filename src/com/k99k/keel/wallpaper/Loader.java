/**
 * 
 */
package com.k99k.keel.wallpaper;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.k99k.keel.wallpaper.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * 首页界面
 * @author keel
 *
 */
public class Loader extends Activity {
	

	private static final String TAG  ="Loader";
	private static final int DIALOG_ERR_WEBCONN = 1;
	private static final int DIALOG_ERR_JSON = DIALOG_ERR_WEBCONN + 1;
	private static final int DIALOG_INFO_NEWAPK = DIALOG_ERR_JSON + 1;
	private static final int DIALOG_ERR_STATE = DIALOG_INFO_NEWAPK + 1;
	
	private static final int LOAD_MSG_REMOTE = 1;
	private static final int LOAD_ERR_REMOTE = LOAD_MSG_REMOTE + 1;
	private static final int LOAD_ERR_LOCAL = LOAD_ERR_REMOTE + 1;
	private static final int LOAD_COMPLET = LOAD_ERR_LOCAL + 1;
	private static final int LOAD_NEW_VERSION = LOAD_COMPLET + 1;
//	private static final int LOAD_OK = LOAD_NEW_VERSION + 1;
	private TextView loadTxt;
//	private View loadView;
	private Button b_enter;
//	private Button b_more;
	

//	/**
//	 * 是否需要远程获取图片
//	 */
//	private static boolean isRemote = true;
//	/**
//	 * json数据中的tag名，是一个String类型的key
//	 */
//	private static String jsonTag = "";
//	/**
//	 * json数据中的cate名，是一个String类型的key
//	 */
//	private static String jsonCate = "";
//	/**
//	 * json数据中的页码标识，是一个int类型的key
//	 */
//	private static int jsonPage = 1;
	
	
	
	/**
	 * json根节点
	 */
	private static JSONObject jsonRoot;
	
	/**
	 * 配置文件json
	 */
	private static JSONObject iniJson;
	
	/**
	 * 图片索引的json String
	 */
	private static String indexJsonStr;
	
	/**
	 * ini的本地JSON文件名
	 */
	private static final String INI_FILE_NAME = "fw_ini.htm";
	
	/**
	 * 远程图片路径的前缀
	 */
	//private static String remotePrePath = "http://202.102.113.204/";
	private static String remoteIndex = "http://202.102.113.204/orion/fw_index.htm";
	private static String stateTxt = "ok";
	
	private static String newVersionTxt = "New version is now available!Check it now?";
	/**
	 * 新版本下载地址
	 */
	private static String newApkUrl = "";

	/**
	 * 消息处理Handler,用于更新界面
	 */
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case LOAD_MSG_REMOTE:
				loadTxt.setText(getString(R.string.txt_loading));
				break;
				
			case LOAD_ERR_REMOTE:
				// 显示使用本地图片的提示框，并说明网络图片才可更新
				loadTxt.setText(getString(R.string.txt_no_net));
				showDialog(DIALOG_ERR_WEBCONN);
				break;
				
//			case DIALOG_ERR_STATE:
//				loadTxt.setText("本地数据不存在,使用初始数据...");
//				showDialog(DIALOG_ERR_STATE);
//				break;
//				
			case LOAD_COMPLET:
				//loadTxt.setText("完成数据载入.");
				try {
					loadTxt.setText(jsonRoot.getString("update"+ID.getLANG()));
					b_enter.setVisibility(View.VISIBLE);
					
					/*
					//处理更多按钮
					final String more = iniJson.getString("more"+lang);
					final String moreUrl = iniJson.getString("moreURL")+"?lang="+lang;
					if (more !=null && (!more.equals("")) && moreUrl != null) {
//						final String[] marr = more.split(",");
//						if (marr.length == 2) {
							b_more.setText(more);
							b_more.setOnClickListener(new OnClickListener(){
								public void onClick(View arg0) {
									openUrl(moreUrl);
								}
					        }); 
							b_more.setVisibility(View.VISIBLE);
//						}
						
					}
					*/
					
				} catch (JSONException e) {
					Log.e(TAG, "jsonRoot update parse error.",e);
				}
				break;
			
		/*	case LOAD_SAVE_OK:
				try {
					loadTxt.setText("图库更新于:"+jsonRoot.getString("update"));
				} catch (JSONException e) {
					Log.e(TAG, "jsonRoot update parse error.",e);
				}
				b_enter.setVisibility(View.VISIBLE);
				break;*/
				
			case LOAD_NEW_VERSION:
				showDialog(DIALOG_INFO_NEWAPK);
				break;

			default:
				break;
			}
		}
	};



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loader);
        this.setTitle(R.string.title);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        loadTxt = (TextView)this.findViewById(R.id.loadingTxt);
//        loadView = this.findViewById(R.id.loading);
        loadTxt.setText(getString(R.string.txt_loadin));
        b_enter = (Button)this.findViewById(R.id.enter);
        b_enter.setText(getString(R.string.bt_enter));
//        b_more = (Button)this.findViewById(R.id.b_more);
        b_enter.setVisibility(View.GONE);
//        b_more.setVisibility(View.GONE);
        b_enter.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent toMain = new Intent();
				toMain.putExtra("jsonRoot", indexJsonStr);//(bundle);
				toMain.putExtra("lang", ID.getLANG());//(bundle);
				toMain.setClass(Loader.this, K99KWall.class);
				startActivity(toMain);
				finish();
				//startActivityForResult(toIndex,0);
			}
        }); 
        //载入
        new LoadingThread().start();
        
    }

    /**
	 * 处理loading状态
	 *
	 */
	private class LoadingThread extends Thread{
		@Override
		public void run() {

	        //初始化ID对象
	        ID.init(Loader.this,Loader.this.getClass().getPackage().getName());
	        
			try {
	        	//先尝试获取本机的files目录下索引
				String iniStr = IO.readTxt(Loader.this,INI_FILE_NAME);
				if (iniStr.equals("")) {
					Log.w(TAG, "Failed to get ini from files.");
					//首次运行,本机无ini,则从raw读取
					iniStr = IO.readRaw(Loader.this,R.raw.fw_ini);
					iniJson = new JSONObject(iniStr);
				}else{
					//与raw下的版本比较
					iniJson = new JSONObject(iniStr);
					String rawIniStr = IO.readRaw(Loader.this,R.raw.fw_ini);
					if (!rawIniStr.equals("")) {
						JSONObject rawIniJson = new JSONObject(rawIniStr);
						int rawIniVersion = rawIniJson.getInt("version");
						Log.d(TAG, "Got ini from file:"+iniJson.getInt("version")+" and from raw:"+rawIniVersion);
						if (rawIniVersion > iniJson.getInt("version")) {
							iniJson = rawIniJson;
						}
					}else{
						Log.w(TAG, "Failed to get ini from raw.");
					}
				}
				
				//轮循获取可用的远程服务器
				JSONArray jarr = iniJson.getJSONArray("server");
				int len = jarr.length();
				String[] servs = new String[len];
				for (int i = 0; i < len; i++) {
					servs[i] = jarr.getString(i);
				}
				NetWork.setServers(servs);
				boolean loadRemoteIniOK = false;
				for (int i = 0; i < len; i++) {
					//String serverOne = jarr.getString(i);
					Log.d(TAG, "TRY TO GET INI FROM:"+NetWork.getServer()+INI_FILE_NAME);
					String remoteIni = NetWork.postUrl(INI_FILE_NAME,ID.getFullJsonEnc());
					//Log.e(TAG, "remoteIni:"+remoteIni);
					
					//成功读取
					if (!remoteIni.equals("")) {
						
						//remotePrePath = serverOne;
						Log.d(TAG, "download remote ini OK:"+NetWork.getServer()+INI_FILE_NAME);
						JSONObject remoteIniJson = new JSONObject(remoteIni);
						//判断服务器状态 ,非ok的服务器跳过
						stateTxt = remoteIniJson.getString("state");
						if (!stateTxt.equals("ok")) {
							Log.w(TAG, "download remote ini failed:"+NetWork.getServer()+INI_FILE_NAME);
							continue;
						}
						//判断程序版本
						Log.d(TAG, "currentVersion:"+ID.getAppVer());
						if (ID.getAppVer() < remoteIniJson.getDouble("apkVersion")) {
							//带上语言参数和包名
							newApkUrl = remoteIniJson.getString("newAPK")+"?lang="+ID.getLANG()+"&pk="+this.getClass().getPackage().getName();
							newVersionTxt = remoteIniJson.getString("newTxt"+ID.getLANG());
							//提示是否下载新版本
							mHandler.sendEmptyMessage(LOAD_NEW_VERSION);
						}
						//判断配置文件版本
						int remoteIniJsonVersion  = remoteIniJson.getInt("version");
						Log.d(TAG, "remoteIniJsonVersion:"+remoteIniJsonVersion);
						if (remoteIniJsonVersion > iniJson.getInt("version")) {
							Log.d(TAG, "update localini to remote ini version:"+iniJson.getInt("version"));
							//远程配置文件版本较新，更新到本地files下
							IO.writeTxt(Loader.this,INI_FILE_NAME,remoteIni);
						}
						//总是使用远端的配置,更新配置
						iniJson = remoteIniJson;
						jarr = iniJson.getJSONArray("server");
						int lens = jarr.length();
						String[] servs2 = new String[lens];
						for (int j = 0; j < lens; j++) {
							servs2[j] = jarr.getString(j);
						}
						NetWork.setServers(servs2);
						remoteIndex = iniJson.getString("index");
						// ini处理完成
						loadRemoteIniOK = true;
						break;
					}else{
						//下一服务器--自动切换
						//NetWork.nextServer();
						//mHandler.sendEmptyMessage(LOAD_ERR_REMOTE);
						continue;
					}
				}
				if (!loadRemoteIniOK) {
					mHandler.sendEmptyMessage(LOAD_ERR_REMOTE);
					return;
				}

//				//处理广告分配比
//				JSONArray jarrads = iniJson.getJSONArray("ads");
//				if (jarrads != null && jarrads.length() == 2) {
//					ID.ads[0]  = jarrads.getInt(0);
//					ID.ads[1]  = jarrads.getInt(1);
//					Log.d(TAG, "ads:"+ID.ads.toString());
//				}
				
					
				Log.d(TAG, "ini version:"+iniJson.getString("version") +"\n remoteIndexPath:"+remoteIndex);
				//----------------------------------				
				//尝试获取远程index
				String reIndexFileName = remoteIndex.substring(remoteIndex.lastIndexOf('/')+1);
				String reIndex = NetWork.getServer()+reIndexFileName;
				for (int i = 0; i < len; i++) {
					//postUrl如果失败会自动切换server
					indexJsonStr = NetWork.postUrl(reIndex,ID.getSmallJsonEnc());
					if (indexJsonStr.equals("")) {
						continue;
					}
				}
				//indexJsonStr = NetWork.postUrl(remoteIndex,ID.getSmallJsonEnc());
				if (indexJsonStr.equals("")) {
					//无法获取远程index
					mHandler.sendEmptyMessage(LOAD_ERR_REMOTE);
					return;
				}
//					jsonTag = "remote";
//		         	jsonCate = "hot";
//		         	jsonPage = 1;
//		         	isRemote = true;
		        Log.d(TAG, "Get the remote index OK!");
				jsonRoot = new JSONObject(indexJsonStr);
				
				//Log.e(TAG, "jsonRoot:"+indexJsonStr);
				Log.d(TAG, "index version:"+jsonRoot.getString("version"));
			} catch (JSONException e) {
				Log.e(TAG, "JSON file parse error.",e);
				mHandler.sendEmptyMessage(LOAD_ERR_REMOTE);
				return;
			}
			
			//loadTxt.setVisibility(View.GONE);
			mHandler.sendEmptyMessage(LOAD_COMPLET);
			 
		}
	}
	
 
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		// 解决横竖屏切换导致重载的问题
		super.onConfigurationChanged(newConfig);
		if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){  
		    //横向  
		    //setContentView(R.layout.file_list_landscape); 
			//Log.i(TAG,"to land...");
		}else{  
		    //竖向  
		    //setContentView(R.layout.file_list);  
			//Log.i(TAG,"to port...");
		}  
	}
	

	@Override
	protected Dialog onCreateDialog(int id) {
		 switch (id) {
	     case DIALOG_ERR_WEBCONN:
	         return new AlertDialog.Builder(Loader.this)
	         .setMessage(getString(R.string.txt_no_net))
	         .setTitle(getString(R.string.err_no_net))
	         .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	             public void onClick(DialogInterface dialog, int whichButton) {
	                 //退出
	            	 finish();
	             }
	         })
	         .create();
	     case DIALOG_ERR_JSON:
	    	 return new AlertDialog.Builder(Loader.this)
	    	 .setMessage(getString(R.string.err_json))
	    	 .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	             public void onClick(DialogInterface dialog, int whichButton) {
	            	 finish();
	             }
	         })
	         .create();
	     case DIALOG_ERR_STATE:
	    	 return new AlertDialog.Builder(Loader.this)
	    	 .setMessage(stateTxt)
	    	 .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	             public void onClick(DialogInterface dialog, int whichButton) {
	            	 finish();
	             }
	         })
	         .create();
	     case DIALOG_INFO_NEWAPK:
	    	 return new AlertDialog.Builder(Loader.this)
	    	 .setMessage(newVersionTxt)
	    	 .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	             public void onClick(DialogInterface dialog, int whichButton) {
	            	 //跳转到下载网页
	            	 NetWork.openUrl(Loader.this,newApkUrl);
	            	 Loader.this.finish();
	             }
	         })
	         .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	             public void onClick(DialogInterface dialog, int whichButton) {
	            	 //
	             }
	         })
	         .create();
		 }
	     return null;
	}
	



}
