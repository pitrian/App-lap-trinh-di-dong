package com.example.appattt.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.appattt.R;
import com.example.appattt.models.ForumPost;
import com.example.appattt.services.ForumFirebaseService;

public class ReplyDialogFragment extends DialogFragment {

    private EditText etReplyContent;
    private Button btnSubmit, btnCancel;

    private String threadId;
    private String parentPostId;
    private ForumFirebaseService forumService;
    private OnReplyPostedListener listener;

    public interface OnReplyPostedListener {
        void onReplyPosted();
    }

    public static ReplyDialogFragment newInstance(String threadId, String parentPostId) {
        ReplyDialogFragment fragment = new ReplyDialogFragment();
        Bundle args = new Bundle();
        args.putString("threadId", threadId);
        args.putString("parentPostId", parentPostId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            threadId = getArguments().getString("threadId");
            parentPostId = getArguments().getString("parentPostId");
        }
        forumService = new ForumFirebaseService();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reply_dialog, container, false);

        etReplyContent = view.findViewById(R.id.etReplyContent);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnCancel = view.findViewById(R.id.btnCancel);

        setupListeners();

        return view;
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> submitReply());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void submitReply() {
        String content = etReplyContent.getText().toString().trim();

        if (content.isEmpty()) {
            etReplyContent.setError("Reply cannot be empty");
            etReplyContent.requestFocus();
            return;
        }

        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(getContext(), "Please login to reply", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        ForumPost post = new ForumPost(threadId, content,
                forumService.getCurrentUserId(),
                forumService.getCurrentUserName(),
                parentPostId);

        btnSubmit.setEnabled(false);

        forumService.createPost(post, new ForumFirebaseService.EmptyCallback() {
            @Override
            public void onSuccess() {
                btnSubmit.setEnabled(true);
                Toast.makeText(getContext(), "Reply posted!", Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    listener.onReplyPosted();
                }

                dismiss();
            }

            @Override
            public void onError(String error) {
                btnSubmit.setEnabled(true);
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setOnReplyPostedListener(OnReplyPostedListener listener) {
        this.listener = listener;
    }
}