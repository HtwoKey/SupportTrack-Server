package com.yibei.supporttrack.entity.vo;

import com.yibei.supporttrack.entity.po.Permission;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class TreeSelect {

    /** 节点ID */
    private String value;

    /** 节点名称 */
    private String label;

    /** 子节点 */
    private List<TreeSelect> children;

    public TreeSelect() {}

    public TreeSelect(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public TreeSelect(String value, String label, List<TreeSelect> children) {
        this.value = value;
        this.label = label;
        this.children = children;
    }

    public TreeSelect(Permission menu)
    {
        this.value = menu.getId().toString();
        this.label = menu.getTitle();
        this.children = menu.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    }


}
