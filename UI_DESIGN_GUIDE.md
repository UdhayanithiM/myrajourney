# MyRa Journey - UI Design Guide

## Design System Overview

This guide provides the design standards and components used throughout the MyRa Journey application.

## Color Palette

### Primary Colors
```
Primary Blue:     #2196F3  (Main brand color)
Primary Dark:     #1976D2  (Darker shade for emphasis)
Secondary Green:  #4CAF50  (Success, positive actions)
Accent Orange:    #FF9800  (Highlights, warnings)
```

### Background Colors
```
Light Background: #F5F5F5  (Main app background)
Card Background:  #FFFFFF  (Card surfaces)
Section Header:   #E3F2FD  (Light blue for headers)
```

### Text Colors
```
Primary Text:     #212121  (Main text)
Secondary Text:   #757575  (Subtitles, descriptions)
Hint Text:        #BDBDBD  (Input hints)
```

### Status Colors
```
Success:          #4CAF50  (Green)
Warning:          #FF9800  (Orange)
Error:            #F44336  (Red)
Info:             #2196F3  (Blue)
```

### Dark Theme Colors
```
Dark Background:  #121212
Dark Surface:     #1E1E1E
Dark Primary:     #1976D2
Dark Text:        #FFFFFF
```

## Typography

### Text Sizes
- **Headers**: 24sp (Page titles)
- **Section Titles**: 18sp (Card headers)
- **Body Text**: 16sp (Buttons, main content)
- **Labels**: 14sp (Field labels)
- **Captions**: 12sp (Small descriptions)
- **Navigation**: 11sp (Bottom nav labels)

### Font Weights
- **Bold**: Headers, section titles, buttons
- **Normal**: Body text, labels

## Spacing & Layout

### Margins
- **Page Padding**: 20dp (Outer container)
- **Card Margins**: 16-20dp (Between cards)
- **Element Spacing**: 16dp (Between elements)
- **Small Spacing**: 4-8dp (Label to field)

### Padding
- **Card Padding**: 20dp (Inside cards)
- **Button Padding**: 14-16dp vertical
- **Input Padding**: 14dp (All sides)
- **Section Header**: 12dp (All sides)

### Dimensions
- **Card Radius**: 16dp (Rounded corners)
- **Button Radius**: 12dp (Rounded corners)
- **Input Radius**: 8dp (Rounded corners)
- **Profile Image**: 120dp × 120dp (Circle)
- **Icons**: 24-28dp (Standard size)
- **Bottom Nav Height**: 70dp

## Components

### 1. Cards (CardView)
```xml
<androidx.cardview.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:elevation="4dp"
    android:radius="16dp">
    <!-- Content -->
</androidx.cardview.widget.CardView>
```

**Usage**: Group related content, create visual hierarchy

### 2. Primary Button
```xml
<Button
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@drawable/bg_button_primary"
    android:textColor="@color/white"
    android:textStyle="bold"
    android:textSize="16sp"
    android:paddingVertical="16dp"
    android:textAllCaps="false"/>
```

**Usage**: Main actions (Submit, Save, Register)

### 3. Secondary Button
```xml
<Button
    android:background="@drawable/bg_button_secondary"
    android:textColor="@color/white"
    android:textStyle="bold"
    android:textSize="16sp"
    android:paddingVertical="14dp"
    android:textAllCaps="false"/>
```

**Usage**: Secondary actions (Upload, Add)

### 4. Input Field with Label
```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Field Label"
    android:textSize="14sp"
    android:textColor="@color/text_secondary"
    android:layout_marginBottom="4dp"/>

<EditText
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Enter value"
    android:textColorHint="@color/text_hint"
    android:textColor="@color/text_primary"
    android:background="@drawable/edit_text_background"
    android:padding="14dp"
    android:layout_marginBottom="16dp"/>
```

**Usage**: All form inputs

### 5. Profile Picture
```xml
<ImageView
    android:layout_width="120dp"
    android:layout_height="120dp"
    android:background="@drawable/bg_profile_circle"
    android:padding="8dp"
    android:scaleType="centerCrop"/>
```

**Usage**: User profile images

### 6. Section Header
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@drawable/bg_section_header"
    android:padding="12dp">
    
    <TextView
        android:text="Section Title"
        android:textSize="18sp"
        android:textStyle="bold"
        android:textColor="@color/primary_color"/>
</LinearLayout>
```

**Usage**: Separate content sections

### 7. Bottom Navigation Item
```xml
<LinearLayout
    android:layout_width="0dp"
    android:layout_weight="1"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:clickable="true"
    android:focusable="true"
    android:background="?attr/selectableItemBackground">
    
    <ImageView
        android:layout_width="24dp"
        android:layout_height="24dp"
        android:tint="@color/primary_color"/>
    
    <TextView
        android:text="Label"
        android:textSize="11sp"
        android:textColor="@color/text_secondary"
        android:layout_marginTop="4dp"/>
</LinearLayout>
```

**Usage**: Bottom navigation tabs

## Page Layouts

### Standard Page Structure
```
ScrollView (background: @color/background_light)
└── LinearLayout (padding: 20dp)
    ├── Header TextView (24sp, bold, centered)
    ├── CardView (Section 1)
    │   └── Content
    ├── CardView (Section 2)
    │   └── Content
    └── Primary Button
```

### Dashboard Structure
```
ConstraintLayout (background: @color/background_light)
├── Top Bar CardView
│   └── Menu, Title, Actions
├── Content Area
│   ├── Illustration/Banner
│   └── Action Cards
└── Bottom Navigation CardView
    └── Navigation Items
```

## Best Practices

### 1. Consistency
- Use the same spacing throughout
- Maintain consistent card styling
- Use defined colors only
- Follow typography scale

### 2. Hierarchy
- Headers: 24sp, bold
- Subheaders: 18sp, bold
- Body: 16sp, normal
- Labels: 14sp, normal

### 3. Accessibility
- Minimum touch target: 48dp
- Color contrast ratio: 4.5:1
- Content descriptions for images
- Proper input types for fields

### 4. Responsiveness
- Use ScrollView for long content
- Use wrap_content for flexible sizing
- Use match_parent for full width
- Use weight for proportional sizing

### 5. Visual Feedback
- Ripple effects on clickable items
- Toast messages for confirmations
- Loading states for async operations
- Error states for validations

## Theme Support

### Light Theme
- White backgrounds
- Dark text on light surfaces
- Blue primary color
- High contrast

### Dark Theme
- Dark backgrounds (#121212)
- Light text on dark surfaces
- Adjusted primary colors
- Reduced brightness

### Implementation
```java
// Apply theme in onCreate
ThemeManager.applyTheme(this);

// Toggle theme
ThemeManager.toggleTheme(this);
recreate();

// Check current theme
boolean isDark = ThemeManager.isDarkMode(this);
```

## Common Patterns

### Form Layout
1. Page header (centered, 24sp)
2. Profile picture card (if applicable)
3. Information card with labeled fields
4. Submit button (full width, primary)

### List Layout
1. Search/filter bar
2. RecyclerView with items
3. Empty state message
4. Floating action button (if needed)

### Details Layout
1. Header with back button
2. Profile/summary card
3. Tabbed or sectioned content
4. Action buttons at bottom

## Icons

### Standard Sizes
- **Navigation Icons**: 24dp
- **Action Icons**: 28dp
- **List Icons**: 20dp

### Tinting
- Primary actions: `@color/primary_color`
- Destructive actions: `@color/error_color`
- Neutral actions: `@color/text_secondary`

## Elevation

### Z-axis Hierarchy
- **Bottom Navigation**: 8dp
- **Top Bar**: 4dp
- **Cards**: 4dp
- **Floating Action Button**: 6dp
- **Dialogs**: 24dp

## Animation Guidelines

### Transitions
- Page transitions: 300ms
- Theme changes: Recreate activity
- Button clicks: Ripple effect
- Card reveals: Fade in

### States
- Loading: Progress indicator
- Success: Green toast
- Error: Red toast
- Empty: Illustration + message

---

**Note**: All measurements in dp (density-independent pixels) and sp (scale-independent pixels) for proper scaling across devices.

**Last Updated**: October 7, 2025
