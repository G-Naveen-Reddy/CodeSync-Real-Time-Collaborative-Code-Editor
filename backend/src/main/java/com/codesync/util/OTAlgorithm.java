package com.codesync.util;

import com.codesync.model.Operation;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Operational Transformation (OT) Algorithm for collaborative text editing.
 * This handles concurrent edits by transforming operations against each other
 * to maintain consistency across all clients.
 */
@Component
public class OTAlgorithm {

    /**
     * Transform operation op1 against op2 so that op1' can be applied after op2.
     * This implements the classic OT transform function.
     */
    public Operation transform(Operation op1, Operation op2) {
        if (op1.getOperationType().equals("INSERT") && op2.getOperationType().equals("INSERT")) {
            return transformInsertInsert(op1, op2);
        } else if (op1.getOperationType().equals("INSERT") && op2.getOperationType().equals("DELETE")) {
            return transformInsertDelete(op1, op2);
        } else if (op1.getOperationType().equals("DELETE") && op2.getOperationType().equals("INSERT")) {
            return transformDeleteInsert(op1, op2);
        } else if (op1.getOperationType().equals("DELETE") && op2.getOperationType().equals("DELETE")) {
            return transformDeleteDelete(op1, op2);
        }
        return op1;
    }

    /**
     * Both operations are inserts. If op1's position is before op2's, no change.
     * If op1's position is after or at op2's position, shift op1 by the length of
     * op2's text.
     */
    private Operation transformInsertInsert(Operation op1, Operation op2) {
        if (op1.getPosition() < op2.getPosition()) {
            return op1; // No transformation needed
        } else {
            Operation transformed = cloneOperation(op1);
            int shift = op2.getText() != null ? op2.getText().length() : 0;
            transformed.setPosition(op1.getPosition() + shift);
            return transformed;
        }
    }

    /**
     * op1 is insert, op2 is delete.
     * If op1's position is before op2's position, no change.
     * If op1's position is at or after op2's position, shift by the delete length.
     */
    private Operation transformInsertDelete(Operation op1, Operation op2) {
        if (op1.getPosition() <= op2.getPosition()) {
            return op1; // Insert before delete, no change
        } else {
            Operation transformed = cloneOperation(op1);
            int deleteLen = op2.getDeleteLength() != null ? op2.getDeleteLength() : 0;
            transformed.setPosition(op1.getPosition() - deleteLen);
            return transformed;
        }
    }

    /**
     * op1 is delete, op2 is insert.
     * If op1's position is before op2's position, no change.
     * If op1's position is at or after op2's position, shift by the insert length.
     */
    private Operation transformDeleteInsert(Operation op1, Operation op2) {
        if (op1.getPosition() < op2.getPosition()) {
            return op1;
        } else {
            Operation transformed = cloneOperation(op1);
            int shift = op2.getText() != null ? op2.getText().length() : 0;
            transformed.setPosition(op1.getPosition() + shift);
            return transformed;
        }
    }

    /**
     * Both operations are deletes.
     * If op1 deletes before op2's range, no change.
     * If op1 deletes within op2's range, adjust.
     * If op1 deletes after op2's range, shift by delete length.
     */
    private Operation transformDeleteDelete(Operation op1, Operation op2) {
        int op1Start = op1.getPosition();
        int op1End = op1.getPosition() + (op1.getDeleteLength() != null ? op1.getDeleteLength() : 0);
        int op2Start = op2.getPosition();
        int op2End = op2.getPosition() + (op2.getDeleteLength() != null ? op2.getDeleteLength() : 0);

        if (op1End <= op2Start) {
            // op1 is entirely before op2's delete range
            return op1;
        } else if (op1Start >= op2End) {
            // op1 is entirely after op2's delete range
            Operation transformed = cloneOperation(op1);
            int shift = op2.getDeleteLength() != null ? op2.getDeleteLength() : 0;
            transformed.setPosition(op1.getPosition() - shift);
            return transformed;
        } else {
            // Overlapping deletes - merge them
            Operation transformed = cloneOperation(op1);
            // Adjust the start position and length
            int newStart = Math.min(op1Start, op2Start);
            int newEnd = Math.max(op1End, op2End);
            transformed.setPosition(newStart);
            transformed.setDeleteLength(newEnd - newStart);
            return transformed;
        }
    }

    /**
     * Include an operation's effects into the document state.
     */
    public String applyOperation(String content, Operation op) {
        return applyOperationStatic(content, op);
    }

    /**
     * Static version of applyOperation for use from static context.
     */
    public static String applyOperationStatic(String content, Operation op) {
        StringBuilder sb = new StringBuilder(content != null ? content : "");

        if (op.getOperationType().equals("INSERT") && op.getText() != null) {
            int pos = Math.min(op.getPosition(), sb.length());
            sb.insert(pos, op.getText());
        } else if (op.getOperationType().equals("DELETE")) {
            int pos = Math.min(op.getPosition(), sb.length());
            int end = Math.min(pos + (op.getDeleteLength() != null ? op.getDeleteLength() : 0), sb.length());
            if (pos < end) {
                sb.delete(pos, end);
            }
        }

        return sb.toString();
    }

    /**
     * Compose two sequential operations into one.
     */
    public Operation compose(Operation op1, Operation op2) {
        if (op1.getOperationType().equals("INSERT") && op2.getOperationType().equals("INSERT")) {
            Operation composed = cloneOperation(op1);
            composed.setText(op1.getText() + op2.getText());
            return composed;
        } else if (op1.getOperationType().equals("DELETE") && op2.getOperationType().equals("DELETE")) {
            Operation composed = cloneOperation(op1);
            int totalDelete = (op1.getDeleteLength() != null ? op1.getDeleteLength() : 0)
                    + (op2.getDeleteLength() != null ? op2.getDeleteLength() : 0);
            composed.setDeleteLength(totalDelete);
            return composed;
        }
        // For mixed types, just return op2 (the most recent)
        return cloneOperation(op2);
    }

    private Operation cloneOperation(Operation op) {
        Operation clone = new Operation();
        clone.setId(op.getId());
        clone.setDocumentId(op.getDocumentId());
        clone.setUserId(op.getUserId());
        clone.setUsername(op.getUsername());
        clone.setOperationType(op.getOperationType());
        clone.setPosition(op.getPosition());
        clone.setText(op.getText());
        clone.setDeleteLength(op.getDeleteLength());
        clone.setVersion(op.getVersion());
        clone.setCreatedAt(op.getCreatedAt());
        return clone;
    }
}
