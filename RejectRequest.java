package org.example;

public class RejectRequest {
    private String reason;

    public RejectRequest() {}

    public RejectRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
