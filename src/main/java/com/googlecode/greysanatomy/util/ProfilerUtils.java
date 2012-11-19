package com.googlecode.greysanatomy.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * ����ͳ�ƹ���
 * @author vlinux
 *
 */
public class ProfilerUtils {

	private static final ThreadLocal<Entry> entryStack = new ThreadLocal<Entry>();

    /**
     * ��ʼ����ͳ��
     * @param message
     */
    public static void start(String message) {
        entryStack.set(new Entry(message, null, null));
    }

    /**
     * ����һ����Ԫ
     */
    public static void enter() {
    	final StackTraceElement stack = Thread.currentThread().getStackTrace()[4];
    	enter(stack.getClassName()+"$"+stack.getMethodName());
    }
    
   /**
    * ����һ����Ԫ
    * @param message
    */
    public static void enter(String message) {
        final Entry currentEntry = getCurrentEntry();
        if (currentEntry != null) {
            currentEntry.enterSubEntry(message);
        }
    }

    /**
     * �ͷ�һ����Ԫ
     */
    public static void release() {
        final Entry currentEntry = getCurrentEntry();
        if (currentEntry != null) {
            currentEntry.release();
        }
    }

    /**
     * �г����е�entry��
     * @return �г�����entry����ͳ�Ƹ�����ռ�õ�ʱ��
     */
    public static String dump() {
    	return dump("", "");
    }

    /**
     * �г����е�entry��
     * @param prefix1 ����ǰ׺
     * @param prefix2 ������ǰ׺
     *
     * @return �г�����entry����ͳ�Ƹ�����ռ�õ�ʱ��
     */
    private static String dump(String prefix1, String prefix2) {
        final Entry entry = entryStack.get();
        return null != entry 
        		? entry.toString(prefix1, prefix2) 
        		: "";
    }

    /**
     * ȡ�õ�һ��entry��
     * @return ��һ��entry����������ڣ��򷵻�<code>null</code>
     */
    public static Entry getEntry() {
        return (Entry) entryStack.get();
    }

    /**
     * ȡ�������һ��entry��
     * @return �����һ��entry����������ڣ��򷵻�<code>null</code>
     */
    private static Entry getCurrentEntry() {
        Entry subEntry = entryStack.get();
        Entry entry = null;
        if (subEntry != null) {
            do {
                entry = subEntry;
                subEntry = entry.getUnreleasedEntry();
            } while (subEntry != null);
        }

        return entry;
    }

    /**
     * ����һ����ʱ��Ԫ��
     */
    public static final class Entry {
    	
		private final List<Entry> subEntries = new ArrayList<Entry>();
		private final String message;
		private final Entry parentEntry;
		private final Entry firstEntry;
		private final long baseTime;
		private final long startTime;
		private long endTime;

        /**
         * ����һ���µ�entry��
         *
         * @param message entry����Ϣ��������<code>null</code>
         * @param parentEntry ��entry��������<code>null</code>
         * @param firstEntry ��һ��entry��������<code>null</code>
         */
        private Entry(String message, Entry parentEntry, Entry firstEntry) {
            this.message     = message;
            this.startTime   = System.currentTimeMillis();
            this.parentEntry = parentEntry;
            this.firstEntry  = (Entry) defaultIfNull(firstEntry, this);
            this.baseTime    = (firstEntry == null) ? 0 : firstEntry.startTime;
        }

        /**
         * ȡ��entry����Ϣ��
         */
        public String getMessage() {
            return message;
        }

        /**
         * ȡ��entry����ڵ�һ��entry����ʼʱ�䡣
         * @return �����ʼʱ��
         */
        public long getStartTime() {
            return (baseTime > 0) ? (startTime - baseTime) : 0;
        }

        /**
         * ȡ��entry����ڵ�һ��entry�Ľ���ʱ�䡣
         * @return ��Խ���ʱ�䣬���entry��δ�������򷵻�<code>-1</code>
         */
        public long getEndTime() {
        	return endTime < baseTime ? -1 : endTime - baseTime; 
        }

        /**
         * ȡ��entry������ʱ�䡣
         * @return entry������ʱ�䣬���entry��δ�������򷵻�<code>-1</code>
         */
        public long getDuration() {
        	return endTime < startTime ? -1 : endTime - startTime; 
        }

		/**
		 * ȡ��entry�������õ�ʱ�䣬����ʱ���ȥ������entry���õ�ʱ�䡣
		 * @return entry�������õ�ʱ�䣬���entry��δ�������򷵻�<code>-1</code>
		 */
		public long getDurationOfSelf() {
			long duration = getDuration();

			if (duration < 0) {
				return -1;
			} else if (subEntries.isEmpty()) {
				return duration;
			} else {
				for (int i = 0; i < subEntries.size(); i++) {
					Entry subEntry = (Entry) subEntries.get(i);
					duration -= subEntry.getDuration();
				}
				return duration < 0 ? -1 : duration;
			}
		}

        /**
         * ȡ�õ�ǰentry�ڸ�entry����ռ��ʱ��ٷֱȡ�
         * @return �ٷֱ�
         */
        public double getPecentage() {
            double parentDuration = 0;
            double duration = getDuration();

            if ((parentEntry != null) && parentEntry.isReleased()) {
                parentDuration = parentEntry.getDuration();
            }

            return (duration > 0) && (parentDuration > 0) 
            		? duration/parentDuration 
            		: 0;
        }

        /**
         * ȡ�õ�ǰentry�ڵ�һ��entry����ռ��ʱ��ٷֱȡ�
         * @return �ٷֱ�
         */
        public double getPecentageOfAll() {
            double firstDuration = 0;
            double duration = getDuration();

            if ((firstEntry != null) && firstEntry.isReleased()) {
                firstDuration = firstEntry.getDuration();
            }

            return (duration > 0) && (firstDuration > 0)
            		? duration/firstDuration
            		: 0;
        }

        /**
         * ������ǰentry������¼����ʱ�䡣
         */
        private void release() {
            endTime = System.currentTimeMillis();
        }

        /**
         * �жϵ�ǰentry�Ƿ������
         * @return ���entry�Ѿ��������򷵻�<code>true</code>
         */
        private boolean isReleased() {
            return endTime > 0;
        }

        /**
         * ����һ���µ���entry��
         * @param message ��entry����Ϣ
         */
        private void enterSubEntry(String message) {
            Entry subEntry = new Entry(message, this, firstEntry);
            subEntries.add(subEntry);
        }

        /**
         * ȡ��δ��������entry��
         * @return δ��������entry�����û����entry��������entry���ѽ������򷵻�<code>null</code>
         */
        private Entry getUnreleasedEntry() {
            Entry subEntry = null;

            if (!subEntries.isEmpty()) {
                subEntry = (Entry) subEntries.get(subEntries.size() - 1);
                if (subEntry.isReleased()) {
                    subEntry = null;
                }
            }

            return subEntry;
        }

        /**
         * ��entryת�����ַ����ı�ʾ��
         * @param prefix1 ����ǰ׺
         * @param prefix2 ������ǰ׺
         * @return �ַ�����ʾ��entry
         */
        private String toString(String prefix1, String prefix2) {
            StringBuffer buffer = new StringBuffer();
            toString(buffer, prefix1, prefix2);
            return buffer.toString();
        }

        /**
         * ��entryת�����ַ����ı�ʾ��
         * @param buffer �ַ���buffer
         * @param prefix1 ����ǰ׺
         * @param prefix2 ������ǰ׺
         */
        private void toString(StringBuffer buffer, String prefix1, String prefix2) {
            buffer.append(prefix1);
            
			String message = getMessage();
			long startTime = getStartTime();
			long duration = getDuration();
			long durationOfSelf = getDurationOfSelf();
			double percent = getPecentage();
			double percentOfAll = getPecentageOfAll();

            /*
             * {0} - entry��Ϣ
             * {1} - ��ʼʱ��
             * {2} - ������ʱ��
             * {3} - �������ĵ�ʱ��
             * {4} - �ڸ�entry����ռ��ʱ�����
             * {5} - ����ʱ�������ɵ�ʱ�����
             */
			Object[] params = new Object[] { 
					message, startTime, duration, durationOfSelf, percent, percentOfAll };

            StringBuffer pattern = new StringBuffer("{1,number} ");

            if (isReleased()) {
                pattern.append("[{2,number}ms");

                if ((durationOfSelf > 0) && (durationOfSelf != duration)) {
                    pattern.append(" ({3,number}ms)");
                }

                if (percent > 0) {
                    pattern.append(", {4,number,##%}");
                }

                if (percentOfAll > 0) {
                    pattern.append(", {5,number,##%}");
                }

                pattern.append("]");
            }

            if (message != null) {
                pattern.append(" - {0}");
            }

            buffer.append(MessageFormat.format(pattern.toString(), params));

            for (int i = 0; i < subEntries.size(); i++) {
                Entry subEntry = (Entry) subEntries.get(i);
                buffer.append('\n');
                if (i == (subEntries.size() - 1)) {
                    subEntry.toString(buffer, prefix2 + "`---", prefix2 + "    "); // ���һ��
                } else if (i == 0) {
                    subEntry.toString(buffer, prefix2 + "+---", prefix2 + "|   "); // ��һ��
                } else {
                    subEntry.toString(buffer, prefix2 + "+---", prefix2 + "|   "); // �м���
                }
            }
        }
    }

    /**
     * �������Ϊ<code>null</code>���򷵻�ָ��Ĭ�϶��󣬷��򷵻ض�����
     * <pre>
     * ObjectUtil.defaultIfNull(null, null)      = null
     * ObjectUtil.defaultIfNull(null, "")        = ""
     * ObjectUtil.defaultIfNull(null, "zz")      = "zz"
     * ObjectUtil.defaultIfNull("abc", *)        = "abc"
     * ObjectUtil.defaultIfNull(Boolean.TRUE, *) = Boolean.TRUE
     * </pre>
     *
     * @param object Ҫ���ԵĶ���
     * @param defaultValue Ĭ��ֵ
     *
     * @return �������Ĭ�϶���
     */
    public static Object defaultIfNull(Object object, Object defaultValue) {
        return (object != null) ? object : defaultValue;
    }

}
