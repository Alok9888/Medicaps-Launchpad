-- V4 Seed: Languages, starter code, companies, and hints for existing questions
-- Note: judge0_language_id values must be verified against real Judge0 GET /languages

-- Languages
INSERT INTO languages (name, judge0_language_id, file_extension, is_active)
VALUES
    ('Python', 71, 'py', TRUE),
    ('Java', 62, 'java', TRUE),
    ('C++', 54, 'cpp', TRUE),
    ('JavaScript', 63, 'js', TRUE),
    ('C', 50, 'c', TRUE),
    ('C#', 51, 'cs', TRUE),
    ('Go', 60, 'go', TRUE),
    ('Rust', 73, 'rs', TRUE)
ON CONFLICT (name) DO NOTHING;

-- Companies
INSERT INTO companies (name)
VALUES
    ('Google'),
    ('Amazon'),
    ('Microsoft'),
    ('Meta'),
    ('Apple'),
    ('Netflix'),
    ('Uber'),
    ('Airbnb'),
    ('LinkedIn'),
    ('Twitter')
ON CONFLICT (name) DO NOTHING;

-- Problem Languages (starter code for each question in each language)
-- Two Sum
INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution:\n    def twoSum(self, nums: List[int], target: int) -> List[int]:\n        pass',
    TRUE
FROM problems p JOIN languages l ON l.name = 'Python' WHERE p.slug = 'two-sum'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        return new int[0];\n    }\n}',
    TRUE
FROM problems p JOIN languages l ON l.name = 'Java' WHERE p.slug = 'two-sum'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution {\npublic:\n    vector<int> twoSum(vector<int>& nums, int target) {\n        return {};\n    }\n};',
    TRUE
FROM problems p JOIN languages l ON l.name = 'C++' WHERE p.slug = 'two-sum'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'var twoSum = function(nums, target) {\n    return [];\n};',
    TRUE
FROM problems p JOIN languages l ON l.name = 'JavaScript' WHERE p.slug = 'two-sum'
ON CONFLICT (problem_id, language_id) DO NOTHING;

-- Binary Search
INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution:\n    def search(self, nums: List[int], target: int) -> int:\n        pass',
    TRUE
FROM problems p JOIN languages l ON l.name = 'Python' WHERE p.slug = 'binary-search'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution {\n    public int search(int[] nums, int target) {\n        return -1;\n    }\n}',
    TRUE
FROM problems p JOIN languages l ON l.name = 'Java' WHERE p.slug = 'binary-search'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution {\npublic:\n    int search(vector<int>& nums, int target) {\n        return -1;\n    }\n};',
    TRUE
FROM problems p JOIN languages l ON l.name = 'C++' WHERE p.slug = 'binary-search'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'var search = function(nums, target) {\n    return -1;\n};',
    TRUE
FROM problems p JOIN languages l ON l.name = 'JavaScript' WHERE p.slug = 'binary-search'
ON CONFLICT (problem_id, language_id) DO NOTHING;

-- Valid Parentheses
INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution:\n    def isValid(self, s: str) -> bool:\n        pass',
    TRUE
FROM problems p JOIN languages l ON l.name = 'Python' WHERE p.slug = 'valid-parentheses'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution {\n    public boolean isValid(String s) {\n        return false;\n    }\n}',
    TRUE
FROM problems p JOIN languages l ON l.name = 'Java' WHERE p.slug = 'valid-parentheses'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution {\npublic:\n    bool isValid(string s) {\n        return false;\n    }\n};',
    TRUE
FROM problems p JOIN languages l ON l.name = 'C++' WHERE p.slug = 'valid-parentheses'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'var isValid = function(s) {\n    return false;\n};',
    TRUE
FROM problems p JOIN languages l ON l.name = 'JavaScript' WHERE p.slug = 'valid-parentheses'
ON CONFLICT (problem_id, language_id) DO NOTHING;

-- Merge Intervals
INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution:\n    def merge(self, intervals: List[List[int]]) -> List[List[int]]:\n        pass',
    TRUE
FROM problems p JOIN languages l ON l.name = 'Python' WHERE p.slug = 'merge-intervals'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution {\n    public int[][] merge(int[][] intervals) {\n        return new int[0][];\n    }\n}',
    TRUE
FROM problems p JOIN languages l ON l.name = 'Java' WHERE p.slug = 'merge-intervals'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution {\npublic:\n    vector<vector<int>> merge(vector<vector<int>>& intervals) {\n        return {};\n    }\n};',
    TRUE
FROM problems p JOIN languages l ON l.name = 'C++' WHERE p.slug = 'merge-intervals'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'var merge = function(intervals) {\n    return [];\n};',
    TRUE
FROM problems p JOIN languages l ON l.name = 'JavaScript' WHERE p.slug = 'merge-intervals'
ON CONFLICT (problem_id, language_id) DO NOTHING;

-- Longest Substring Without Repeating Characters
INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution:\n    def lengthOfLongestSubstring(self, s: str) -> int:\n        pass',
    TRUE
FROM problems p JOIN languages l ON l.name = 'Python' WHERE p.slug = 'longest-substring-without-repeating-characters'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution {\n    public int lengthOfLongestSubstring(String s) {\n        return 0;\n    }\n}',
    TRUE
FROM problems p JOIN languages l ON l.name = 'Java' WHERE p.slug = 'longest-substring-without-repeating-characters'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'class Solution {\npublic:\n    int lengthOfLongestSubstring(string s) {\n        return 0;\n    }\n};',
    TRUE
FROM problems p JOIN languages l ON l.name = 'C++' WHERE p.slug = 'longest-substring-without-repeating-characters'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, starter_code, enabled)
SELECT p.id, l.id,
    'var lengthOfLongestSubstring = function(s) {\n    return 0;\n};',
    TRUE
FROM problems p JOIN languages l ON l.name = 'JavaScript' WHERE p.slug = 'longest-substring-without-repeating-characters'
ON CONFLICT (problem_id, language_id) DO NOTHING;

-- Problem Companies (associate questions with companies)
INSERT INTO problem_companies (problem_id, company_id, frequency)
SELECT p.id, c.id, 5
FROM problems p JOIN companies c ON c.name = 'Google' WHERE p.slug = 'two-sum'
ON CONFLICT (problem_id, company_id) DO NOTHING;

INSERT INTO problem_companies (problem_id, company_id, frequency)
SELECT p.id, c.id, 4
FROM problems p JOIN companies c ON c.name = 'Amazon' WHERE p.slug = 'two-sum'
ON CONFLICT (problem_id, company_id) DO NOTHING;

INSERT INTO problem_companies (problem_id, company_id, frequency)
SELECT p.id, c.id, 3
FROM problems p JOIN companies c ON c.name = 'Microsoft' WHERE p.slug = 'binary-search'
ON CONFLICT (problem_id, company_id) DO NOTHING;

INSERT INTO problem_companies (problem_id, company_id, frequency)
SELECT p.id, c.id, 4
FROM problems p JOIN companies c ON c.name = 'Meta' WHERE p.slug = 'valid-parentheses'
ON CONFLICT (problem_id, company_id) DO NOTHING;

INSERT INTO problem_companies (problem_id, company_id, frequency)
SELECT p.id, c.id, 3
FROM problems p JOIN companies c ON c.name = 'Apple' WHERE p.slug = 'merge-intervals'
ON CONFLICT (problem_id, company_id) DO NOTHING;

INSERT INTO problem_companies (problem_id, company_id, frequency)
SELECT p.id, c.id, 2
FROM problems p JOIN companies c ON c.name = 'Google' WHERE p.slug = 'longest-substring-without-repeating-characters'
ON CONFLICT (problem_id, company_id) DO NOTHING;

-- Problem Hints
INSERT INTO problem_hints (problem_id, hint_order, content)
SELECT p.id, 1, 'Use a hash map to store numbers and their indices for O(1) lookups.'
FROM problems p WHERE p.slug = 'two-sum'
ON CONFLICT (problem_id, hint_order) DO NOTHING;

INSERT INTO problem_hints (problem_id, hint_order, content)
SELECT p.id, 2, 'The complement of nums[i] is target - nums[i]. Check if it exists in the map.'
FROM problems p WHERE p.slug = 'two-sum'
ON CONFLICT (problem_id, hint_order) DO NOTHING;

INSERT INTO problem_hints (problem_id, hint_order, content)
SELECT p.id, 1, 'Binary search works on sorted arrays. Check middle element and eliminate half.'
FROM problems p WHERE p.slug = 'binary-search'
ON CONFLICT (problem_id, hint_order) DO NOTHING;

INSERT INTO problem_hints (problem_id, hint_order, content)
SELECT p.id, 1, 'Use a stack to track opening brackets. Match each closing bracket with the top of stack.'
FROM problems p WHERE p.slug = 'valid-parentheses'
ON CONFLICT (problem_id, hint_order) DO NOTHING;

INSERT INTO problem_hints (problem_id, hint_order, content)
SELECT p.id, 1, 'Sort intervals by start time first. Then merge overlapping ones in a single pass.'
FROM problems p WHERE p.slug = 'merge-intervals'
ON CONFLICT (problem_id, hint_order) DO NOTHING;

INSERT INTO problem_hints (problem_id, hint_order, content)
SELECT p.id, 1, 'Use a sliding window with a set/dictionary to track characters in current window.'
FROM problems p WHERE p.slug = 'longest-substring-without-repeating-characters'
ON CONFLICT (problem_id, hint_order) DO NOTHING;

INSERT INTO problem_hints (problem_id, hint_order, content)
SELECT p.id, 2, 'When a duplicate is found, shrink the window from the left until the duplicate is removed.'
FROM problems p WHERE p.slug = 'longest-substring-without-repeating-characters'
ON CONFLICT (problem_id, hint_order) DO NOTHING;