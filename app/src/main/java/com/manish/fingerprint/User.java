package com.manish.fingerprint;

import com.mantra.mfs100.FingerData;

/**
 * Created by manish on 24-03-2018.
 */

public class User {
    public String useraadhaarno;
    public FingerData userfingerdata;
    User(String useraadhaarno,FingerData userfingerdata )
    {
        this.useraadhaarno = useraadhaarno;
        this.userfingerdata = userfingerdata;

    }
    User()
    {
        this.useraadhaarno = "00000000000";
        this.userfingerdata = null;
    }
}
