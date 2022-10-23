package com.nowcoder.community.service;

import java.util.Date;

public interface DataService {
    void saveUV(String ip);
    Long getUVNum(Date start, Date end);
    void saveDAU(int userId);
    long getDAUNum(Date start, Date end);
}
