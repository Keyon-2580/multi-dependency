import time
import math
import json
import os
import re
import difflib

measure_index_file = 'result/MeasureIndex.csv'
clone_group_file = 'result/type123_method_group_result.csv'
clone_similarity_file = 'result/type123_method_result.csv'
token_data_folder = 'tokenData'
state_file = 'result/state.json'
output_file = 'similarity.csv'
start_offset = -2147483647
comment_pattern1 = re.compile('//.*?\n', re.S)
comment_pattern2 = re.compile('/\*.*?\*/', re.S)
space_pattern = re.compile('\s', re.S)
null_string_pattern = re.compile('[\r\n\f]{2,}', re.S)
first_null_string_pattern = re.compile('^\n', re.S)


def init_measure_dict():
    """初始化方法列表"""
    global token_data_folder
    measure_info = dict()
    measure_list_files = list()
    for root, dirs, files in os.walk(token_data_folder):
        for file in files:
            if file.startswith('MeasureList'):
                measure_list_files.append(os.path.join(root, file))
    measure_list_files.sort()
    idx = 0
    for file in measure_list_files:
        with open(file, 'r') as f:
            for line in f.readlines():
                tmp = line.strip().split(',')
                measure = dict()
                measure['start_token'] = int(tmp[2])
                measure['end_token'] = int(tmp[3])
                measure_info[str(idx)] = measure
                idx += 1
    return measure_info


def init_measure_indecies():
    """初始化MeasureIndex.csv里的数据"""
    measure_indecies = dict()
    with open(measure_index_file, 'r') as f:
        for line in f.readlines():
            tmp = line.strip().split(',')
            mid = tmp[0]
            measure_indecies[mid] = tmp[1:]
    return measure_indecies


def read_lines(file, start_line, end_line):
    """读取文件指定行"""
    lines = list()
    try:
        if file.endswith('.java') or file.endswith('.JAVA'):
            with open(file, 'r', encoding='utf8') as f:
                lines = f.readlines()
        else:
            with open(file, 'r') as f:
                lines = f.readlines()
        lines = lines[start_line - 1: end_line]
    except BaseException as e:
        pass
    return lines


def remove_comments(code):
    """移除java,c,c++代码里的注释，移除代码内的空格"""
    tmp = comment_pattern1.sub('', code)
    tmp = comment_pattern2.sub('', tmp)
    tmp = space_pattern.sub('', tmp)
    return tmp


def remove_comments_and_mull_string(code_lines):
    """移除java,c,c++代码里的注释，移除代码内的空行"""
    tmp = comment_pattern1.sub('', code_lines)
    tmp = comment_pattern2.sub('', tmp)
    tmp = null_string_pattern.sub('\n', tmp)
    tmp = first_null_string_pattern.sub('', tmp)
    return tmp.split('\n')



def get_equal_rate(str1, str2):
   return difflib.SequenceMatcher(None, str1, str2).quick_ratio()


def get_code_type(code1, code2):
    """获取代码克隆类型"""
    return 1 if code1 == code2 else 2


def init_clone_groups():
    """初始化克隆组数据"""
    global clone_group_file
    clone_groups = list()
    with open(clone_group_file, 'r') as f:
        for line in f.readlines():
            ids = line.strip().split(',')
            clone_groups.append(ids)
    return clone_groups


def init_clone_similarity_result():
    """初始化克隆队相似度"""
    global clone_similarity_file
    clone_similarity_result = list()
    with open(clone_similarity_file, 'r') as f:
        for line in f.readlines():
            ids = line.strip().split(',')
            clone_similarity_result.append(ids)
    return clone_similarity_result


def init_state():
    """初始化状态数据"""
    global state_file
    with open(state_file, 'r') as f:
        state = json.load(f)
    return state


def suffix_array_similarity(tokens1, tokens2):
    """用后缀数组计算相似度"""
    measure1 = Measure(0, len(tokens1) - 1)
    measure2 = Measure(len(tokens1), len(tokens1) + len(tokens2) - 1)
    measures = [measure1, measure2]

    tokens = list()
    tokens.extend(tokens1)
    tokens.extend(tokens2)
    sa = SuffixArray(tokens)
    res = sa.process()

    clonePairs = list()
    for pair in res:
        first_from = search_index(measures, pair[0])
        first_to = search_index(measures, pair[0] + pair[2] - 1)
        second_from = search_index(measures, pair[1])
        second_to = search_index(measures, pair[1] + pair[2] - 1)
        if first_from == second_from:
            continue
        if first_from != first_to or second_from != second_to:
            continue
        if pair[2] == 0:
            continue
        x1 = min(pair[0], pair[1])
        x2 = max(pair[0], pair[1])
        clonePairs.append(ClonePair(x1, x2, pair[2]))

    # 计算方法内连续片段的长度
    cover = calc_cover_length(clonePairs)
    return cover / max(len(tokens1), len(tokens2))


def search_index(measures, pos):
    """搜索子串所在的方法索引"""
    idx = -1
    for i in range(len(measures)):
        if pos >= measures[i].start and pos <= measures[i].end:
            idx = i
            break
    return idx


def calc_cover_length(pairs):
    """计算重叠片段长度"""
    pairs = sorted(pairs, key=lambda x: x.first)
    idx = 0
    total_size = 0
    start_token = 0
    size = 0
    while idx < len(pairs):
        if idx == 0:
            start_token = pairs[idx].first
            size = pairs[idx].size
            idx += 1
            continue
        if start_token + size >= pairs[idx].first:
            if start_token + size >= pairs[idx].first + pairs[idx].size:
                pass
            else:
                size = pairs[idx].first - start_token + pairs[idx].size
            idx += 1
        else:
            total_size += size
            start_token = pairs[idx].first
            size = pairs[idx].size
            idx += 1
    total_size += size
    return total_size


def calc_small_file_clone_similarity(measures, measure_indecies, state):
    token_size = 30
    small_file_measure_indecies = dict()
    small_file_token_index = dict()

    for key, value in measure_indecies.items():
        measure = measures[key]
        measure_file_id, offset = calc_file_id(state, measure['start_token'])
        measure_file = '%s/allTokenCsv%d' % (token_data_folder, measure_file_id)
        tokens = read_tokens(measure_file, offset, measure['end_token'] - measure['start_token'])
        if token_size > len(tokens) > 0:
            small_file_measure_indecies[key] = value
            small_file_token_index[key] = len(tokens)

    small_file_token_index_sorted = sorted(small_file_token_index.items(), key=lambda d: d[1])
    #small_file_sorted_keys = list(small_file_token_index_sorted.keys())

    print('less than 30 token file clone, size: %d\n' % len(small_file_token_index_sorted))
    small_file_clone_similarity = list()
    cnt = 0
    for i in range(0, len(small_file_token_index_sorted) - 1):
        print('%.2f%%' % (i * 100.0 / len(small_file_token_index_sorted)))
        token_i = small_file_token_index_sorted[i][1]
        for j in range(i+1, len(small_file_token_index_sorted)):
            token_j = small_file_token_index_sorted[j][1]
            if token_j + token_i < 16 and token_j - token_i > 5:
                break
            if token_j - token_i > 10:
                break
            similarity = suffix_array_similarity(token_i, token_j)
            if similarity < 0.6:
                continue
            key_i = small_file_token_index_sorted[i][0]
            key_j = small_file_token_index_sorted[j][0]
            m1 = measure_indecies[key_i]
            m2 = measure_indecies[key_j]
            lines1 = read_lines(m1[0], int(m1[1]), int(m1[2]))
            lines2 = read_lines(m2[0], int(m2[1]), int(m2[2]))
            code1 = remove_comments('\n'.join(lines1))
            code2 = remove_comments('\n'.join(lines2))
            str_similarity = get_equal_rate(code1, code2)
            if str_similarity < 0.6:
                continue

            code_lines1 = remove_comments_and_mull_string('\n'.join(lines1))
            code_lines2 = remove_comments_and_mull_string('\n'.join(lines2))
            lines1_size = len(lines1) + 1
            lines2_size = len(lines2) + 1
            code_lines1_size = len(code_lines1)
            code_lines2_size = len(code_lines2)

            if 1 != int(similarity):
                value = CloneSimilarityData(key_i, key_j, similarity, 3, lines1_size, lines2_size,
                                    code_lines1_size, code_lines2_size)
                small_file_clone_similarity.append(value)
            else:
                if len(lines1) == 0 or len(lines2) == 0:
                    value = CloneSimilarityData(key_i, key_j, similarity, 2, lines1_size, lines2_size,
                                        code_lines1_size, code_lines2_size)
                    small_file_clone_similarity.append(value)
                code_type = get_code_type(code1, code2)
                value = CloneSimilarityData(key_i, key_j, similarity, code_type, lines1_size, lines2_size,
                                    code_lines1_size, code_lines2_size)
                small_file_clone_similarity.append(value)

            cnt += 1

    return small_file_clone_similarity


def calc_clone_group_similarity_values(measures, measure_indecies, state, group):
    clone_similarity_values = list()
    for i in range(0, len(group) - 1):
        measure1 = measures[group[i]]
        measure1_file_id, offset1 = calc_file_id(state, measure1['start_token'])
        measure1_file = '%s/allTokenCsv%d' % (token_data_folder, measure1_file_id)
        tokens1 = read_tokens(measure1_file, offset1, measure1['end_token'] - measure1['start_token'])
        if len(tokens1) > 50000:
            print('clone file/method pass, token size: %d\n' % len(tokens1))
            continue
        for j in range(i + 1, len(group)):
            measure2 = measures[group[j]]
            measure2_file_id, offset2 = calc_file_id(state, measure2['start_token'])
            measure2_file = '%s/allTokenCsv%d' % (token_data_folder, measure2_file_id)
            tokens2 = read_tokens(measure2_file, offset2, measure2['end_token'] - measure2['start_token'])
            if len(tokens2) > 50000:
                print('clone file/method pass, token size: %d\n' % len(tokens2))
                continue
            similarity = suffix_array_similarity(tokens1, tokens2)
            if similarity < 0.6:
                continue
            m1 = measure_indecies[group[i]]
            m2 = measure_indecies[group[j]]
            lines1 = read_lines(m1[0], int(m1[1]), int(m1[2]))
            lines2 = read_lines(m2[0], int(m2[1]), int(m2[2]))
            code_lines1 = remove_comments_and_mull_string('\n'.join(lines1))
            code_lines2 = remove_comments_and_mull_string('\n'.join(lines2))
            lines1_size = len(lines1) + 1
            lines2_size = len(lines2) + 1
            code_lines1_size = len(code_lines1)
            code_lines2_size = len(code_lines2)

            if 1 != int(similarity):
                value = CloneSimilarityData(group[i], group[j], similarity, 3, lines1_size, lines2_size,
                                            code_lines1_size, code_lines2_size)
                clone_similarity_values.append(value)
            else:
                if len(lines1) == 0 or len(lines2) == 0:
                    value = CloneSimilarityData(group[i], group[j], similarity, 2, lines1_size, lines2_size,
                                                code_lines1_size, code_lines2_size)
                    clone_similarity_values.append(value)
                code1 = remove_comments('\n'.join(lines1))
                code2 = remove_comments('\n'.join(lines2))
                code_type = get_code_type(code1, code2)
                value = CloneSimilarityData(group[i], group[j], similarity, code_type, lines1_size, lines2_size,
                                            code_lines1_size, code_lines2_size)
                clone_similarity_values.append(value)

    return clone_similarity_values


def process():
    """计算每个克隆组中每个克隆实例之间的相似度"""
    global token_data_folder, start_offset, measure_index_file
    measures = init_measure_dict()
    measure_indecies = init_measure_indecies()
    clone_groups = init_clone_groups()
    state = init_state()
    clone_similarity_value = list()

    #print('less than 30 token file clone:\n')
    #clone_similarity_value += calc_small_file_clone_similarity(measures, measure_indecies, state)

    #value_test = CloneSimilarityData('0', '0', 0.0, 0, 0, 0, 0, 0)
    #clone_similarity_value.append(value_test)

    size = len(clone_groups)
    cnt = 0
    print('clone group size: %d\n' % size)
    for group in clone_groups:
        cnt += 1
        print('%.2f%%' % (cnt * 100.0 / size))

        # 过滤掉克隆实例特别多的克隆组
        if len(group) > 200:
            print('clone group pass, id : %d, size: %d\n' % (cnt - 1, len(group)))
            continue

        clone_similarity_value += calc_clone_group_similarity_values(measures, measure_indecies, state, group)

    with open(output_file, 'w') as f:
        for data in clone_similarity_value:
            f.write('%s,%s,%f,%d,%d,%d,%d,%d\n' %
                    (data.clone_unit1, data.clone_unit2, data.similarity, data.clone_type,
                     data.lines1_size, data.lines2_size, data.loc1, data.loc2))


def calc_file_id(state, start_token):
    """计算方法的token所在的文件以及偏移"""
    global start_offset
    token_file_num = len(state['tokenIndexList'])
    if token_file_num == 1:
        return (0, start_token - start_offset)
    idx = -1
    offset = -1
    for i in range(token_file_num - 1):
        current_offset = state['tokenIndexList'][i]
        next_offset = state['tokenIndexList'][i + 1]
        if start_token >= current_offset and start_token < next_offset:
            idx = i
            offset = start_token - current_offset
            break
    idx = token_file_num - 1 if idx == -1 else idx
    offset = start_token - state['tokenIndexList'][-1] if offset == -1 else offset
    return (idx, offset)


class SuffixArray():
    def __init__(self, tokens):
        self.tokens = tokens
        self.sa = list()
        self.height = list()

    def _build_sa(self):
        all_suffixes = list()
        for i in range(len(self.tokens)):
            all_suffixes.append(self.tokens[i:])
        all_suffixes.sort()
        for i in range(len(all_suffixes)):
            self.sa.append(len(self.tokens) - len(all_suffixes[i]))

    def _calc_height(self):
        self.height.append(0)
        for i in range(1, len(self.tokens)):
            s1 = self.tokens[self.sa[i - 1]:]
            s2 = self.tokens[self.sa[i]:]
            h = 0
            size = min(len(s1), len(s2))
            for i in range(size):
                if s1[i] != s2[i]:
                    break
                h += 1
            self.height.append(h)

    def process(self):
        self._build_sa()
        self._calc_height()
        res = list()
        for i in range(1, len(self.height)):
            res.append((self.sa[i - 1], self.sa[i], self.height[i]))
        return res


class Measure():
    def __init__(self, start, end):
        self.start = start
        self.end = end


class ClonePair():
    def __init__(self, first, second, size):
        self.first = first
        self.second = second
        self.size = size


class CloneSimilarityData():
    def __init__(self, clone_unit1, clone_unit2, similarity, clone_type, lines1_size, lines2_size, loc1, loc2):
        self.clone_unit1 = clone_unit1
        self.clone_unit2 = clone_unit2
        self.similarity = similarity
        self.clone_type = clone_type
        self.lines1_size = lines1_size
        self.lines2_size = lines2_size
        self.loc1 = loc1
        self.loc2 = loc2


def read_tokens(file, offset, size):
    """读取保存在磁盘上的方法的token"""
    with open(file, 'rb') as f:
        f.seek(offset)
        content = f.read(size)
    tokens = list()
    for token in content:
        tokens.append(token)
    return tokens


if __name__ == '__main__':
    process()