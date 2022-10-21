package cn.edu.fudan.se.multidependency.model;

/**
 * @description:
 * @author: keyon
 * @time: 2022/10/21 10:03
 */

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

public class MethodMetric {
    String relativeFilePath;
    String methodFullName;
    int methodCcn;
    int ncss;

    public String getRelativeFilePath() {
        return this.relativeFilePath;
    }

    public String getMethodFullName() {
        return this.methodFullName;
    }

    public int getMethodCcn() {
        return this.methodCcn;
    }

    public int getNcss() {
        return this.ncss;
    }

    public void setRelativeFilePath(String relativeFilePath) {
        this.relativeFilePath = relativeFilePath;
    }

    public void setMethodFullName(String methodFullName) {
        this.methodFullName = methodFullName;
    }

    public void setMethodCcn(int methodCcn) {
        this.methodCcn = methodCcn;
    }

    public void setNcss(int ncss) {
        this.ncss = ncss;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof MethodMetric)) {
            return false;
        } else {
            MethodMetric other = (MethodMetric)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.getMethodCcn() != other.getMethodCcn()) {
                return false;
            } else if (this.getNcss() != other.getNcss()) {
                return false;
            } else {
                label40: {
                    Object this$relativeFilePath = this.getRelativeFilePath();
                    Object other$relativeFilePath = other.getRelativeFilePath();
                    if (this$relativeFilePath == null) {
                        if (other$relativeFilePath == null) {
                            break label40;
                        }
                    } else if (this$relativeFilePath.equals(other$relativeFilePath)) {
                        break label40;
                    }

                    return false;
                }

                Object this$methodFullName = this.getMethodFullName();
                Object other$methodFullName = other.getMethodFullName();
                if (this$methodFullName == null) {
                    if (other$methodFullName != null) {
                        return false;
                    }
                } else if (!this$methodFullName.equals(other$methodFullName)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof MethodMetric;
    }

    @Override
    public int hashCode() {
        int PRIME = 1;
        int result = 1;
        result = result * 59 + this.getMethodCcn();
        result = result * 59 + this.getNcss();
        Object $relativeFilePath = this.getRelativeFilePath();
        result = result * 59 + ($relativeFilePath == null ? 43 : $relativeFilePath.hashCode());
        Object $methodFullName = this.getMethodFullName();
        result = result * 59 + ($methodFullName == null ? 43 : $methodFullName.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "MethodMetric(relativeFilePath=" + this.getRelativeFilePath() + ", methodFullName=" + this.getMethodFullName() + ", methodCcn=" + this.getMethodCcn() + ", ncss=" + this.getNcss() + ")";
    }

    public MethodMetric() {
    }

    public MethodMetric(String relativeFilePath, String methodFullName, int methodCcn, int ncss) {
        this.relativeFilePath = relativeFilePath;
        this.methodFullName = methodFullName;
        this.methodCcn = methodCcn;
        this.ncss = ncss;
    }
}
