//package com.webtoon.pattern;
//
//import java.util.Set;
//
//public interface Subject {
//    String getSubjectId();     // 알림 주체 식별자 (작가 ID or 작품 ID)
//    String getSubjectName();   // 알림 주체 표시명 (작가명 or 작품명)
//
//    void attach(String userId);        // 팔로우
//    void detach(String userId);        // 언팔로우
//    Set<String> getFollowerUserIds();  // 팔로워 조회
//}

//package com.webtoon.pattern;
//
//import com.webtoon.domain.Reader;
//
//import java.util.List;
//
///**
// * 팔로우 가능한 대상 (웹툰, 작가 등)
// */
//public interface Subject {
//
//    /** 알림 주체 식별자 (작품/작가 ID 등) */
//    String getSubjectId();
//
//    /** 알림 주체 표시명 (작품명/작가명 등) */
//    String getSubjectName();
//
//    /** 팔로우 */
//    void attach(Reader reader);
//
//    /** 언팔로우 */
//    void detach(Reader reader);
//
//    /** 팔로워 목록 */
//    List<Reader> getFollowers();
//}


package com.webtoon.pattern;

import java.util.Set;

public interface Subject {
    String getSubjectId();     // 알림 주체 식별자 (작가 ID or 작품 ID)
    String getSubjectName();   // 알림 주체 표시명 (작가명 or 작품명)

    void attach(String userId);        // 팔로우
    void detach(String userId);        // 언팔로우

    Set<String> getFollowerUserIds();  // 팔로워 조회

    /** Observer 패턴 핵심 메서드: 주체가 팔로워들에게 알림을 보내는 역할 */
    void notifyObservers();
}
