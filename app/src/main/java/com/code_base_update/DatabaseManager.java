package com.code_base_update;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.code_base_update.beans.ApplicationBean;
import com.code_base_update.beans.CollegeBean;
import com.code_base_update.beans.ComplaintBean;
import com.code_base_update.beans.HostelNoticeBean;
import com.code_base_update.beans.Student;
import com.code_base_update.interfaces.DataCallback;
import com.code_base_update.interfaces.SuccessCallback;
import com.code_base_update.utility.InputHelper;
import com.code_base_update.utility.SessionManager;
import com.code_base_update.utility.UserManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class DatabaseManager {

    public static final String COMPLAINT_FOLDER = "Complaints";
    private static final String COMPLAINT_TYPES = "ComplaintList";
    private static final String APPLICATION_FOLDER = "Applications";
    private static final String NOTICE_FOLDER = "Notices";
    private DatabaseReference mDatabase;
    private SessionManager session;
    private Context context;

    public DatabaseManager(Context context) {
        session = new SessionManager(context);
        mDatabase = getBaseRef(context);
        this.context = context;
        prepareOfflineAccessLocations();
    }

    private void prepareOfflineAccessLocations() {
        FirebaseDatabase.getInstance().getReference().child(COMPLAINT_TYPES).keepSynced(true);
        mDatabase.child(DatabaseManager.APPLICATION_FOLDER).keepSynced(true);
        mDatabase.child(DatabaseManager.COMPLAINT_FOLDER).keepSynced(true);
    }

    public ArrayList<ComplaintBean> loadAllComplaint() {
        ArrayList<ComplaintBean> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ComplaintBean comp = new ComplaintBean();
            comp.setComplaintId("id_" + i);
            list.add(comp);
        }
        //TODO how to fetch data from mDatabase
        return list;

    }

    public void saveApplication(final SuccessCallback callback, ApplicationBean applicationToSave) {
        callback.onInitiated();
        mDatabase.child(DatabaseManager.APPLICATION_FOLDER).child(new UserManager().getUID()).child(applicationToSave.getApplicationId() + "").
                setValue(applicationToSave).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) callback.onSuccess();
                else callback.onFailure(task.getException().getLocalizedMessage());

            }
        });
    }

    public void loadAllApplication(final DataCallback<ArrayList<ApplicationBean>> dataCallback) {
        final ArrayList<ApplicationBean> list= new ArrayList<>();
        mDatabase.child(DatabaseManager.APPLICATION_FOLDER).child(new UserManager().getUID()).
                orderByChild("timeStamp").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            list.clear();
                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                list.add(d.getValue(ApplicationBean.class));
                            }
                            dataCallback.onSuccess(list);
                        }else dataCallback.onFailure("No data found");

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dataCallback.onError(databaseError.getMessage());
                    }
                });
    }

    public void registerComplaint(final SuccessCallback successCallback, ComplaintBean complaintToRegister) {
        successCallback.onInitiated();
        mDatabase.child(DatabaseManager.COMPLAINT_FOLDER).child(new UserManager().getUID()).child(complaintToRegister.getComplaintId()).
                setValue(complaintToRegister).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) successCallback.onSuccess();
                else successCallback.onFailure(task.getException().getLocalizedMessage());

            }
        });

    }

    public void loadComplaintTypes(final DataCallback<ArrayList<String>> dataCallback) {

        final ArrayList<String> types = new ArrayList<>();
        types.add("Select problem field");
        FirebaseDatabase.getInstance().getReference()
                .child(COMPLAINT_TYPES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        types.add(d.getKey());
                    }
                    dataCallback.onSuccess(types);
                } else dataCallback.onFailure("No data found");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dataCallback.onError(databaseError.getMessage());
            }
        });

    }

    public static DatabaseReference getBaseRef(Context context) {
        SessionManager session = new SessionManager(context);
        return FirebaseDatabase.getInstance().getReference(session.getCollegeId()).child(session.getHostelId());
    }

    public void loadSubDomains(String domain, final DataCallback<ArrayList<String>> callback) {

        FirebaseDatabase.getInstance().getReference().child(COMPLAINT_TYPES).child(domain).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ArrayList<String> types = new ArrayList<>();
                    for (DataSnapshot d : dataSnapshot.getChildren())
                        types.add(d.getValue().toString());
                    callback.onSuccess(types);
                } else callback.onFailure("No data found");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void markComplaintAsResolved(ComplaintBean complaintBean, final SuccessCallback successCallback) {
        successCallback.onInitiated();
        mDatabase.child(COMPLAINT_FOLDER).child(new UserManager().getUID()).child(complaintBean.getComplaintId()).setValue(complaintBean).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    successCallback.onSuccess();
                else
                    successCallback.onFailure(task.getException().getLocalizedMessage());
            }
        });


    }

    public void loadAllColleges(final DataCallback<ArrayList<CollegeBean>> callback) {
        FirebaseDatabase.getInstance().getReference().child(Constants.COLLEGE_LIST)
                .orderByChild("collegeName")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<CollegeBean> collegeBeans = new ArrayList<>();
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                collegeBeans.add(d.getValue(CollegeBean.class));
                            }
                            callback.onSuccess(collegeBeans);
                        } else callback.onFailure("No data found");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onFailure(databaseError.getMessage());
                    }
                });

    }

    public void registerStudent(Student studentDetails, final SuccessCallback callback) {
        callback.onInitiated();
        mDatabase = getBaseRef(context);
        mDatabase.child(Constants.STUDENT_LIST).child(studentDetails.getEmail().replace(".","dot")).setValue(studentDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                    callback.onSuccess();
                else callback.onFailure(task.getException().getLocalizedMessage());

            }
        });
    }

    public void isStudentEnrolled(final Student studentDetails, final SuccessCallback callback) {
        callback.onInitiated();
        //Double check college and hostel id
        if(TextUtils.isEmpty(studentDetails.getCollegeId())||TextUtils.isEmpty(studentDetails.getHostelId())){
            callback.onFailure("Incorrect college and hostel");
            return;
        }
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child(studentDetails.getCollegeId()).child(studentDetails.getHostelId()).child(Constants.ENROLLED_STUDENT_LIST);
        mRef.child(studentDetails.getAdharNo()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.child("email").getValue().toString().equals(studentDetails.getEmail())
                    &&dataSnapshot.child("mobile").getValue().toString().equals(studentDetails.getMobileNo())){
                        callback.onSuccess();
                        saveCollegeAndHostelIds(studentDetails.getCollegeId(),studentDetails.getHostelId());
                    }
                    else callback.onFailure("Credentials don't match with database, please contact warden");

                }else callback.onFailure("Student doesn't exists, please contact your warden");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError.getMessage());
            }
        });


    }

    private void saveCollegeAndHostelIds(String collegeId, String hostelId) {
        session.setCollegeId(collegeId);
        session.setHostelId(hostelId);
    }

    public void fetchStudent(String email, final DataCallback<Student> callback) {
        email = InputHelper.removeDot(email);
        final String finalEmail = email;
        FirebaseDatabase.getInstance().getReference().child(Constants.USER_LIST).child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    try {
                        session.setCollegeId(dataSnapshot.child(Constants.COLLEGE_ID).getValue().toString());
                        session.setHostelId(dataSnapshot.child(Constants.HOSTEL_ID).getValue().toString());
                        mDatabase = getBaseRef(context);
                        mDatabase.child(Constants.STUDENT_LIST).child(finalEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    Student student = dataSnapshot.getValue(Student.class);
                                    callback.onSuccess(student);

                                }else
                                    callback.onFailure("Data missing in college database, contact warden");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                callback.onError(databaseError.getMessage());
                            }
                        });

                    }catch (Exception e){
                        callback.onFailure(e.getLocalizedMessage());
                    }
                }else callback.onFailure("User not exists");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });


    }

    public void saveStudent(Student studentDetails) {
        //TODO make it more reliable
        String formattedEmail = InputHelper.removeDot(studentDetails.getEmail());
        FirebaseDatabase.getInstance().getReference().child(Constants.USER_LIST).child(formattedEmail)
                .child(Constants.COLLEGE_ID).setValue(studentDetails.getCollegeId());
        FirebaseDatabase.getInstance().getReference().child(Constants.USER_LIST).child(formattedEmail)
                .child(Constants.HOSTEL_ID).setValue(studentDetails.getHostelId());


    }

    public void getCollege(String collegeId, final DataCallback<CollegeBean> callback) {
        FirebaseDatabase.getInstance().getReference().child(Constants.COLLEGE_LIST)
                .child(collegeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    callback.onSuccess(dataSnapshot.getValue(CollegeBean.class));
                }else callback.onFailure("College not found");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError.getMessage());
            }
        });

    }

    public void sendApplicationReminder(ApplicationBean item, final SuccessCallback callback) {
        item.setReminder();
        mDatabase.child(APPLICATION_FOLDER).child(new UserManager().getUID()).child(item.getApplicationId()+"")
                .setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) callback.onSuccess();
                else callback.onFailure(task.getException().getLocalizedMessage());

            }
        });
    }

    public void withdrawApplication(ApplicationBean item, final SuccessCallback callback) {
        item.setWithdrawn();
        mDatabase.child(APPLICATION_FOLDER).child(new UserManager().getUID()).child(item.getApplicationId()+"")
                .setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) callback.onSuccess();
                else callback.onFailure(task.getException().getLocalizedMessage());

            }
        });

    }

    public void saveNotice(HostelNoticeBean notice, final SuccessCallback callback) {
        callback.onInitiated();
        mDatabase.child(NOTICE_FOLDER).child(notice.getNoticeId()).setValue(notice).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) callback.onSuccess();
                else callback.onFailure(task.getException().getLocalizedMessage());
            }
        });
    }
}
