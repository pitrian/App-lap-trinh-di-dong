package com.example.appattt.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.appattt.R;
import com.example.appattt.models.ForumPost;
import com.example.appattt.services.ForumFirebaseService;

public class ReplyDialogFragment extends DialogFragment {

    private static final String ARG_THREAD_ID = "thread_id";
    private static final String ARG_PARENT_ID = "parent_id";
    private static final String ARG_CATEGORY_ID = "category_id";

    private String threadId;
    private String parentId;
    private String categoryId;

    private ForumFirebaseService forumService;
    private OnReplyPostedListener listener;

    public interface OnReplyPostedListener {
        void onReplyPosted();
    }

    public static ReplyDialogFragment newInstance(String threadId, String parentId) {
        ReplyDialogFragment fragment = new ReplyDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_THREAD_ID, threadId);
        args.putString(ARG_PARENT_ID, parentId);
        fragment.setArguments(args);
        return fragment;
    }

    public static ReplyDialogFragment newInstance(String threadId, String parentId, String categoryId) {
        ReplyDialogFragment fragment = new ReplyDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_THREAD_ID, threadId);
        args.putString(ARG_PARENT_ID, parentId);
        args.putString(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            threadId = getArguments().getString(ARG_THREAD_ID);
            parentId = getArguments().getString(ARG_PARENT_ID);
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
        }
        forumService = new ForumFirebaseService();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_reply, null);

        EditText etContent = view.findViewById(R.id.etContent);

        builder.setView(view)
                .setTitle(parentId == null ? "Reply to Thread" : "Reply to Comment")
                .setPositiveButton("Post", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String content = etContent.getText().toString().trim();

                        if (content.isEmpty()) {
                            Toast.makeText(getActivity(), "Please enter a reply", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!forumService.isUserAuthenticated()) {
                            Toast.makeText(getActivity(), "Please login to reply", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // SỬA: Tạo ForumPost với 4 tham số như constructor yêu cầu
                        ForumPost post = new ForumPost(
                                threadId,
                                content,
                                forumService.getCurrentUserId(),
                                forumService.getCurrentUserName()
                        );

                        // Set các thuộc tính bổ sung
                        if (parentId != null) {
                            post.setParentId(parentId);
                            post.setDepth(1); // Độ sâu của reply là 1
                        }

                        if (categoryId != null) {
                            post.setCategoryId(categoryId);
                        }

                        forumService.createPost(post, new ForumFirebaseService.EmptyCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getActivity(), "Reply posted", Toast.LENGTH_SHORT).show();
                                if (listener != null) {
                                    listener.onReplyPosted();
                                }
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(getActivity(), "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null);

        return builder.create();
    }

    public void setOnReplyPostedListener(OnReplyPostedListener listener) {
        this.listener = listener;
    }
}