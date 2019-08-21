package kr.co.gubed.habit2good;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapps.android.share.AdInfoKey;
import com.mapps.android.view.EndingAdView;
import com.mz.common.listener.AdListener;

public class EndingDialog extends Dialog {
	private TextView mTitleView;
	private Button mLeftButton;
	private Button mRightButton;
	private String mTitle;
	private EndingAdView m_flexibleAD = null;

	private View.OnClickListener mLeftClickListener;
	private View.OnClickListener mRightClickListener;

	private String p, m, s;

	private int mediaType;

	public void setCode(int p, int m, int s, int mediaType) {
		this.p = String.valueOf(p);
		this.m = String.valueOf(m);
		this.s = String.valueOf(s);
		this.mediaType = mediaType;
	}

	public EndingDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
	}

	public EndingDialog(Context context, String title, View.OnClickListener singleListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mTitle = title;
		this.mLeftClickListener = singleListener;
	}

	public EndingDialog(Context context, String title, View.OnClickListener leftListener, View.OnClickListener rightListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mTitle = title;
		this.mLeftClickListener = leftListener;
		this.mRightClickListener = rightListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.8f;
		getWindow().setAttributes(lpWindow);

		setContentView(R.layout.ending_dialog);

		setLayout();
		setTitle(mTitle);
		setClickListener(mLeftClickListener, mRightClickListener);
	}

	private void setTitle(String title) {
		mTitleView.setText(title);
	}

	private void setClickListener(View.OnClickListener left, View.OnClickListener right) {
		if (left != null && right != null) {
			mLeftButton.setOnClickListener(left);
			mRightButton.setOnClickListener(right);
		} else if (left != null && right == null) {
			mLeftButton.setOnClickListener(left);
		} else {

		}
	}

	private void setLayout() {
		FrameLayout ending_ad = (FrameLayout) findViewById(R.id.ending_ad);
		mTitleView = (TextView) findViewById(R.id.tv_title);
		mLeftButton = (Button) findViewById(R.id.bt_left);
		mRightButton = (Button) findViewById(R.id.bt_right);
		m_flexibleAD = new EndingAdView(getContext(), mediaType);
		m_flexibleAD.setAdViewCode(p, m, s);
		m_flexibleAD.setBannerSize(320, 480);
		m_flexibleAD.setAdListener(new AdListener() {

			public void onFailedToReceive(View arg0, int errorCode) {
				// TODO Auto-generated method stub
				Log.e(getClass().getName(), "-------> Ending_errorCode=" + errorCode);
				showErrorMsg(errorCode);
			}

			public void onChargeableBannerType(View arg0, boolean arg1) {
				// TODO Auto-generated method stub
			}

			public void onInterClose(View v) {
				// TODO Auto-generated method stub

			}

			public void onAdClick(View v) {
				// TODO Auto-generated method stub

			}
		});
		/*m_flexibleAD.setAccount("id");
		m_flexibleAD.setEmail("few.com");*/
		ending_ad.addView(m_flexibleAD);
		if (m_flexibleAD != null) {
			m_flexibleAD.startEndingAdView(); // windowID가 flexible과 다른 경우에 셋팅
		}
	}

	@Override
	public void dismiss() {
		if (m_flexibleAD != null) {
			m_flexibleAD.onDestroy();
		}
		super.dismiss();
	}

	public EndingAdView getM_flexibleAD() {
		return m_flexibleAD;
	}

	public void setM_flexibleAD(EndingAdView m_flexibleAD) {
		this.m_flexibleAD = m_flexibleAD;
	}

	private void showErrorMsg(int errorCode) {
		String log;
		switch (errorCode) {
		case AdInfoKey.AD_SUCCESS:
			log = "[ " + errorCode + " ] " + "광고 성공";
			break;
		case AdInfoKey.AD_ID_NO_AD:
			log = "[ " + errorCode + " ] " + "광고 소진";
			break;
		case AdInfoKey.NETWORK_ERROR:
			log = "[ " + errorCode + " ] " + "(ERROR)네트워크";
			break;
		case AdInfoKey.AD_SERVER_ERROR:
			log = "[ " + errorCode + " ] " + "(ERROR)서버";
			break;
		case AdInfoKey.AD_API_TYPE_ERROR:
			log = "[ " + errorCode + " ] " + "(ERROR)API 형식 오류";
			break;
		case AdInfoKey.AD_APP_ID_ERROR:
			log = "[ " + errorCode + " ] " + "(ERROR)ID 오류";
			break;
		case AdInfoKey.AD_WINDOW_ID_ERROR:
			log = "[ " + errorCode + " ] " + "(ERROR)ID 오류";
			break;
		case AdInfoKey.AD_ID_BAD:
			log = "[ " + errorCode + " ] " + "(ERROR)ID 오류";
			break;
		case AdInfoKey.AD_CREATIVE_ERROR:
			log = "[ " + errorCode + " ] " + "(ERROR)광고 생성 불가";
			break;
		case AdInfoKey.AD_ETC_ERROR:
			log = "[ " + errorCode + " ] " + "(ERROR)예외 오류";
			break;
		case AdInfoKey.CREATIVE_FILE_ERROR:
			log = "[ " + errorCode + " ] " + "(ERROR)파일 형식";
			break;
		case AdInfoKey.AD_INTERVAL:
			log = "[ " + errorCode + " ] " + "광고 요청 어뷰징";
			break;
		case AdInfoKey.AD_TIMEOUT:
			log = "[ " + errorCode + " ] " + "광고 API TIME OUT";
			break;
		case AdInfoKey.AD_ADCLICK:
			log = "[ " + errorCode + " ] " + "광고 Click";
			break;
		default:
			log = "[ " + errorCode + " ] " + "etc";
			break;
		}
		debug(log);
	}

	private void debug(String msg) {
		//Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
		Log.e(getClass().getName(), msg);
	}
}
