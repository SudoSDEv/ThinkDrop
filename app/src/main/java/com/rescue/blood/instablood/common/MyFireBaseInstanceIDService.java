package com.rescue.blood.instablood.common;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.rescue.blood.instablood.common.Common;
import com.rescue.blood.instablood.common.Token;

/**
 * Created by sayan07 on 14/3/18.
 */

public class MyFireBaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        Common.TOKEN=FirebaseInstanceId.getInstance().getToken();
        Log.d("token1",Common.TOKEN);
        Common.updateToServer(Common.TOKEN);

    }


}
