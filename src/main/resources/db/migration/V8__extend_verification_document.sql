-- 서류 인증 화면 보강.

-- '인증 가능한 서류' 4종(학생증/재학증명서/입학허가서/성적증명서) 구분.
ALTER TABLE verification_document ADD COLUMN document_type VARCHAR(30);

-- 반려 사유. 디자인의 '서류 인증 실패' 화면에는 표시 영역이 없지만, 사유를 안 주면
-- 학생이 같은 서류를 계속 재제출하게 되므로 서버는 반드시 저장/반환한다.
ALTER TABLE verification_document ADD COLUMN reject_reason VARCHAR(500);

UPDATE verification_document SET document_type = 'STUDENT_ID' WHERE document_type IS NULL;
ALTER TABLE verification_document ALTER COLUMN document_type SET NOT NULL;
