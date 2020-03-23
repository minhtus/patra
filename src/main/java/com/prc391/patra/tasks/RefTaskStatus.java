package com.prc391.patra.tasks;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
enum RefTaskStatus {
    PENDING(0), DOING(1), SUBMITTED(2), DONE(3);

    @Id
    private int statusId;
    private String status;

    RefTaskStatus(int statusId) {
        this.statusId = statusId;
        this.status = this.name();
    }

//    public static RefTaskStatus fromValue(int value) {
//        if (value >= 0) {
//            for (RefTaskStatus status : values()) {
//                if (status.statusId == status) {
//                    return status;
//                }
//            }
//        }
//        return getDefault();
//    }
//
//    public static RefTaskStatus getDefault() {
//        return PENDING;
//    }
}
