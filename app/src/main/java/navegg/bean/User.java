package navegg.bean;

import com.google.gson.annotations.SerializedName;


/**
 * Created by william on 08/06/17.
 */

public class User {

    @SerializedName("status")
    private String mStatus;
    @SerializedName("nvgid")
    private long mNvgId;

    private int accountId;


    public User() {
    }


    public long getmNvgId() {
        return mNvgId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int codConta) {
        this.accountId = codConta;
    }

    public long getId (){ return this.mNvgId;}
}
