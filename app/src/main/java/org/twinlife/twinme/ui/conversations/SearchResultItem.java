/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.conversations;

import androidx.annotation.NonNull;

import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.ui.mainActivity.ConversationsFragment;
import org.twinlife.twinme.ui.mainActivity.UIConversation;
import org.twinlife.twinme.ui.users.UIContact;

import java.util.Objects;

class SearchResultItem {

    enum Type {
        HEADER(0),
        FOOTER(1),
        CONTACT(2),
        GROUP(3),
        MESSAGE(4);

        final int index;

        Type(int index) {
            this.index = index;
        }

    }

    static class Header extends SearchResultItem {
        final ConversationsFragment.SearchFilter section;
        final String title;

        Header(ConversationsFragment.SearchFilter section, String title) {
            super(Type.HEADER);
            this.section = section;
            this.title = title;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Header header = (Header) o;
            return section == header.section && Objects.equals(title, header.title);
        }

        @Override
        public int hashCode() {
            return Objects.hash(section, title);
        }
    }

    static class Footer extends SearchResultItem {
        private final ConversationsFragment.SearchFilter searchFilter;

        Footer(ConversationsFragment.SearchFilter searchFilter) {
            super(Type.FOOTER);
            this.searchFilter = searchFilter;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Footer header = (Footer) o;
            return searchFilter == header.searchFilter;
        }

        @Override
        public int hashCode() {
            return Objects.hash(searchFilter);
        }
    }

    static class Conversation extends SearchResultItem {
        @NonNull
        final UIConversation uiConversation;
        @NonNull
        final String searchContent;

        Conversation(@NonNull Type type, @NonNull UIConversation uiConversation, @NonNull String searchContent) {
            super(type);
            this.uiConversation = uiConversation;
            this.searchContent = searchContent;
        }

        @Override
        boolean hasSameContent(SearchResultItem other) {
            Conversation otherConv = (Conversation) other;

            if (!searchContent.equals(otherConv.searchContent)) {
                // Highlighting will be different
                return false;
            }

            ConversationService.Descriptor descriptor = uiConversation.getLastDescriptor();
            ConversationService.Descriptor otherDescriptor = otherConv.uiConversation.getLastDescriptor();

            if (descriptor == null ^ otherDescriptor == null) {
                return false;
            }

            if (descriptor == null) {
                UIContact contact = uiConversation.getUIContact();
                UIContact otherContact = otherConv.uiConversation.getUIContact();
                return Objects.equals(contact.getContact().getId(), otherContact.getContact().getId());
            }

            if (descriptor.getType() != otherDescriptor.getType()) {
                return false;
            }

            if (descriptor.getUpdatedTimestamp() != otherDescriptor.getUpdatedTimestamp()) {
                return false;
            }

            if (descriptor.getType() == ConversationService.Descriptor.Type.OBJECT_DESCRIPTOR) {
                String message = ((ConversationService.ObjectDescriptor) descriptor).getMessage();
                String otherMessage = ((ConversationService.ObjectDescriptor) otherDescriptor).getMessage();
                return Objects.equals(message, otherMessage);
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Conversation that = (Conversation) o;

            ConversationService.DescriptorId descriptorId = uiConversation.getLastDescriptor() != null ? uiConversation.getLastDescriptor().getDescriptorId() : null;
            ConversationService.DescriptorId thatDescriptorId = that.uiConversation.getLastDescriptor() != null ? that.uiConversation.getLastDescriptor().getDescriptorId() : null;

            return Objects.equals(uiConversation.getConversationId(), that.uiConversation.getConversationId()) &&
                    Objects.equals(descriptorId, thatDescriptorId);
        }

        @Override
        public int hashCode() {
            ConversationService.DescriptorId descriptorId = uiConversation.getLastDescriptor() != null ? uiConversation.getLastDescriptor().getDescriptorId() : null;
            return Objects.hash(uiConversation.getConversationId(), descriptorId);
        }
    }

    @NonNull
    final Type type;

    SearchResultItem(@NonNull Type type) {
        this.type = type;
    }

    boolean isSameItem(SearchResultItem other) {
        return equals(other);
    }

    boolean hasSameContent(SearchResultItem other) {
        return equals(other);
    }
}
