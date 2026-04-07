package com.picknquicks.domain.category;
import com.picknquicks.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_category_slug", columnList = "slug"),
                @Index(name = "idx_category_parent", columnList = "parent_id"),
                @Index(name = "idx_category_active", columnList = "active"),
                @Index(name = "idx_category_display_order", columnList = "display_order")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_slug", columnNames = "slug")
        }
)
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"parent", "children"})
@ToString(exclude = {"parent", "children"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseEntity {

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "meta_title", length = 128)
    private String metaTitle;

    @Column(name = "meta_description", length = 255)
    private String metaDescription;

    @Column(name = "meta_keywords", length = 255)
    private String metaKeywords;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(
            mappedBy = "parent",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    @OrderBy("displayOrder ASC")
    private Set<Category> children = new HashSet<>();

    public boolean isRoot() {
        return parent == null;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public int getLevel() {
        int level = 0;
        Category current = this.parent;
        while (current != null) {
            level++;
            current = current.parent;
        }
        return level;
    }

    public void addChild(Category child) {
        if (children == null) {
            children = new HashSet<>();
        }
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(Category child) {
        if (children != null) {
            children.remove(child);
            child.setParent(null);
        }
    }

    public String getFullPath() {
        if (isRoot()) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }

    public Set<Category> getAncestors() {
        Set<Category> ancestors = new HashSet<>();
        Category current = this.parent;
        while (current != null) {
            ancestors.add(current);
            current = current.parent;
        }
        return ancestors;
    }

    public boolean isAncestorOf(Category category) {
        Category current = category.parent;
        while (current != null) {
            if (current.equals(this)) {
                return true;
            }
            current = current.parent;
        }
        return false;
    }

    public boolean isDescendantOf(Category category) {
        return category.isAncestorOf(this);
    }
}