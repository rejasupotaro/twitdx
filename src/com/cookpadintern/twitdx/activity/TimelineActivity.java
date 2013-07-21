package com.cookpadintern.twitdx.activity;

import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.*;

import com.cookpadintern.twitdx.R;
import com.cookpadintern.twitdx.common.Const;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.ImageButton;


public class TimelineActivity extends Activity implements OnClickListener {
    private static SharedPreferences mSharedPreferences;
    private static TwitterStream mTwitterStream;
    private static Twitter mTwitter;
    
    private ScrollView mScrollView;
    private TextView mTextView;
    private LinearLayout mMenu;
    private LinearLayout mContent;
    private LinearLayout.LayoutParams mContentParams;
    private TranslateAnimation mSlide;
    private ImageButton mMenuBtn;
    private Button mTimelineBtn;
    private Button mMentionBtn;
    private Button mLogoutBtn;
    private Button mAboutBtn;
    
    
    private int mMenuWidth = 0;
    private int mCurrentScreenId;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mTextView = (TextView) findViewById(R.id.tweetText);
        mMenu = (LinearLayout) findViewById(R.id.menu);
        mContent = (LinearLayout)findViewById(R.id.content);
        mTimelineBtn = (Button) findViewById(R.id.btn_timeline);
        mMentionBtn = (Button) findViewById(R.id.btn_mention);
        mAboutBtn = (Button) findViewById(R.id.btn_about);
        mLogoutBtn = (Button) findViewById(R.id.btn_logout);
        mCurrentScreenId = R.id.btn_timeline;
        
        mMenuWidth = mMenu.getLayoutParams().width;       
        mContentParams = (LinearLayout.LayoutParams)mContent.getLayoutParams();
        mContentParams.width = this.getResources().getDisplayMetrics().widthPixels; //getWindowManager().getDefaultDisplay().getWidth();
        mContentParams.leftMargin = -mMenuWidth;
        mContent.setLayoutParams(mContentParams);
        // find and set listener for btn_menu
        mMenuBtn = (ImageButton)findViewById(R.id.menu_button);
        
        mMenuBtn.setOnClickListener(this);
        mTimelineBtn.setOnClickListener(this);
        mMentionBtn.setOnClickListener(this);
        mAboutBtn.setOnClickListener(this);
        mLogoutBtn.setOnClickListener(this);
        
        mSharedPreferences = getSharedPreferences(Const.PREFERENCE_NAME, MODE_PRIVATE);
        if (!isOnline()) {
            startActivityForResult(new Intent(TimelineActivity.this, LoginActivity.class), Const.LOGIN_REQUEST);
        } else {
            String oauthAccessToken = mSharedPreferences.getString(Const.PREF_KEY_TOKEN, "");
            String oAuthAccessTokenSecret = mSharedPreferences.getString(Const.PREF_KEY_SECRET, "");

            ConfigurationBuilder confbuilder = new ConfigurationBuilder();
            Configuration conf = confbuilder
                    .setOAuthConsumerKey(Const.CONSUMER_KEY)
                    .setOAuthConsumerSecret(Const.CONSUMER_SECRET)
                    .setOAuthAccessToken(oauthAccessToken)
                    .setOAuthAccessTokenSecret(oAuthAccessTokenSecret).build();
            
            // first fetch current timeline
            mTwitter = new TwitterFactory(conf).getInstance();
            List<Status> statuses;
            try {
                statuses = mTwitter.getHomeTimeline();
                setTimelineToView(statuses);
                
                // then stream
                mTwitterStream = new TwitterStreamFactory(conf).getInstance();
                startStreamingTimeline();
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //goto login
                
            }
        }
    }

   

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case Const.LOGIN_REQUEST:
            break;
        default:
            break;
        }
    }
    
    @Override
    public void onClick(View v) {
        Button c = (Button)findViewById(mCurrentScreenId);
        switch(v.getId()) {
        case R.id.btn_about:
            c.setTextColor(Color.parseColor(getString(R.string.BtnTextNormalColor)));
            mAboutBtn.setTextColor(Color.parseColor(getString(R.string.BtnTextPressedColor)));
            mCurrentScreenId = v.getId();
            // go to about
            break;
        case R.id.btn_timeline:
            c.setTextColor(Color.parseColor(getString(R.string.BtnTextNormalColor)));
            mTimelineBtn.setTextColor(Color.parseColor(getString(R.string.BtnTextPressedColor)));
            mCurrentScreenId = v.getId();
            //refresh time line
            break;
        case R.id.btn_mention:
            c.setTextColor(Color.parseColor(getString(R.string.BtnTextNormalColor)));
            mMentionBtn.setTextColor(Color.parseColor(getString(R.string.BtnTextPressedColor)));
            mCurrentScreenId = v.getId();
            //go to mention screen
            break;
        case R.id.btn_logout:
            c.setTextColor(Color.parseColor(getString(R.string.BtnTextNormalColor)));
            mLogoutBtn.setTextColor(Color.parseColor(getString(R.string.BtnTextPressedColor)));
            mCurrentScreenId = mTimelineBtn.getId();
            logOut();
            break;

        }
        int marginX, animateFromX, animateToX = 0;
        // menu is hidden
        if(mContentParams.leftMargin == -mMenuWidth) {
            animateFromX = 0;
            animateToX = mMenuWidth;
            marginX = 0;
        } else { // menu is visible
            animateFromX = 0;
            animateToX = -mMenuWidth;
            marginX = -mMenuWidth;
        }
        slideMenuIn(animateFromX, animateToX, marginX);
    }

    private void slideMenuIn(int animateFromX, int animateToX, final int marginX) {
        mSlide = new TranslateAnimation(animateFromX, animateToX, 0, 0);
        mSlide.setDuration(200);
        mSlide.setFillEnabled(true);
        mSlide.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                mContentParams.setMargins(marginX, 0, 0, 0);
                mContent.setLayoutParams(mContentParams);
            }
            public void onAnimationRepeat(Animation animation) { }
            public void onAnimationStart(Animation animation) { }
        });
        mContent.startAnimation(mSlide);        
    }

    private boolean isOnline() {
        return mSharedPreferences.getString(Const.PREF_KEY_TOKEN, null) != null;
    }
    
    private void setTimelineToView(List<Status> statuses) {
        for (Status status : statuses) {
            final String tweet = "@" + status.getUser().getScreenName() + " : " + status.getText() + "\n";
            mTextView.append(tweet);
            mScrollView.fullScroll(View.FOCUS_DOWN);
        }       
    }
    
    private void logOut() {
        // TODO Auto-generated method stub
        
    }
   
    public void startStreamingTimeline() {
        UserStreamListener listener = new UserStreamListener() {
            @Override
            public void onStatus(Status status) {
                final String tweet = "@" + status.getUser().getScreenName() + " : " + status.getText() + "\n"; 
                mTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.append(tweet);
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });              
            }
            
            @Override
            public void onUserListSubscription(User arg0, User arg1, UserList arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onScrubGeo(long arg0, long arg1) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onStallWarning(StallWarning arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onTrackLimitationNotice(int arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onException(Exception arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onBlock(User arg0, User arg1) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onDeletionNotice(long arg0, long arg1) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onDirectMessage(DirectMessage arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onFavorite(User arg0, User arg1, Status arg2) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onFollow(User arg0, User arg1) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onFriendList(long[] arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onUnblock(User arg0, User arg1) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onUnfavorite(User arg0, User arg1, Status arg2) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onUserListCreation(User arg0, UserList arg1) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onUserListDeletion(User arg0, UserList arg1) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onUserListMemberAddition(User arg0, User arg1, UserList arg2) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onUserListMemberDeletion(User arg0, User arg1, UserList arg2) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onUserListUnsubscription(User arg0, User arg1, UserList arg2) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onUserListUpdate(User arg0, UserList arg1) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onUserProfileUpdate(User arg0) {
                // TODO Auto-generated method stub
                
            }
        };
        mTwitterStream.addListener(listener);
        mTwitterStream.user();
    }
}
