package api.navegg.mylib.naveggUtils.bean;

import com.google.gson.annotations.SerializedName;


/**
 * Created by william on 08/06/17.
 */

public class User {

    @SerializedName("status")
    private String mStatus;
    @SerializedName("nvgid")
    private long mNvgId;

    private int codConta;


    public User() {
    }

    public String getmStatus() {
        return mStatus;
    }

    public void setmStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    public long getmNvgId() {
        return mNvgId;
    }

    public void setmNvgId(long mNvgId) {
        this.mNvgId = mNvgId;
    }

    public int getCodConta() {
        return codConta;
    }

    public void setCodConta(int codConta) {
        this.codConta = codConta;
    }
}
