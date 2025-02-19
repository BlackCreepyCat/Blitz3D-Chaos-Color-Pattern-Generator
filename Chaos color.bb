
;-----------------------------------------------------------
;        Note: Could do with optimizing
;-----------------------------------------------------------

        AppTitle "Chaotic Colors"

;-----------------------------------------------------------
; Globals
;----------

        Global gfx_width = 1024
        Global gfx_height = 768
        Global gfx_widthM1 = gfx_width - 1
        Global gfx_heightM1 = gfx_height - 1
        Global gfx_depth = 0
        Global gfx_window = 2
        
        Global Mode = 0
        Global MaxMode = 3
        
        Global colcount = 0
        Global ccSpeed = 0
        
        Global Rand_Delay = 0
        Global Rand_Delay_Time = 150
        
        ; Screen Buffer
        Dim screenMap( gfx_width, gfx_height )
        
        ; Sin Cos counters and counter frequency
        Dim Counters(8)
        Dim Frequency(8)
        
        ; Spectrum color storage
        Dim Spec(1536)
        
        ; Sin Cos Lookup table
        Dim SinP#(360)
        Dim CosP#(360)
        

        
;-----------------------------------------------------------


;-----------------------------------------------------------
; Initialzize
;----------

        Print ""
        Print "    Press R now for resizable window or"
        Print "  ?----- Run Fullscreen ( "+gfx_width+" x "+gfx_height+" ) Y/N ?" 
        WaitKey
        gfx_window = 2
        If KeyHit(21) Then gfx_window = 1
        If KeyHit(19) Then gfx_window = 3

        ; Setup random counter and frequency 
        initializeCounters()
        
        ; setup spectrum array
        Spectrum()
        
        ; Set up Sin/Cos lookup table
        For i=0 To 360
                SinP#(i)=Sin#(i)
                CosP#(i)=Cos#(i)
        Next
        
        ; Looks like were ready
        Graphics gfx_width, gfx_height, gfx_depth, gfx_window
        SetBuffer( BackBuffer() )
        
;-----------------------------------------------------------
        
        

;===========================================================
; Main loop
;===========================================================

        While Not KeyDown(1)
        Cls
                inc_Counters()
                
                Rand_Delay = Rand_Delay + 1
                If Rand_Delay > Rand_Delay_Time
                        Rand_Delay = 0
                        initializeCounters()
                EndIf 
                
                ; Randomize counter and frequency 
                If KeyHit(57) Then initializeCounters()
                
                LockBuffer( BackBuffer() )
                
                ; loop through all the pixels on screen
                For y = 0 To gfx_heightM1
                        For x = 0 To gfx_widthM1
                                ; funky sin & cos stuff just randomly put together, seems to work ok :)
                                If ( Mode = 0 )
                                        dx =  x + (  CosP#( (Counters(0) + x)Mod 360 ) * SinP#( (Counters(1) + x)Mod 360) * CosP#( (Counters(2) + y)Mod 360) * SinP#( (Counters(3) + y)Mod 360)) * 40 
                                        dy =  y + (  SinP#( (Counters(4) + y)Mod 360 ) * CosP#( (Counters(5) + y)Mod 360) * SinP#( (Counters(6) + x)Mod 360) * CosP#( (Counters(7) + x)Mod 360)) * 40        
                                ElseIf ( Mode = 1 )
                                        dx =  x + (  CosP#( (Counters(0) + x)Mod 360 ) * SinP#( (Counters(1) + x)Mod 360) - CosP#( (Counters(2) + y)Mod 360) * SinP#( (Counters(3) + y)Mod 360)) * 40 
                                        dy =  y + (  SinP#( (Counters(4) + y)Mod 360 ) * CosP#( (Counters(5) + y)Mod 360) - SinP#( (Counters(6) + x)Mod 360) * CosP#( (Counters(7) + x)Mod 360)) * 40        
                                ElseIf ( Mode = 2 )
                                        dx =  x + (  CosP#( (Counters(0) + x)Mod 360 ) + CosP#( (Counters(1) + x)Mod 360) - SinP#( (Counters(2) + y)Mod 360) - CosP#( (Counters(3) + y)Mod 360)) * 30 
                                        dy =  y + (  SinP#( (Counters(4) + y)Mod 360 ) - SinP#( (Counters(5) + y)Mod 360) - SinP#( (Counters(6) + x)Mod 360) + CosP#( (Counters(7) + x)Mod 360)) * 30
                                ElseIf ( Mode = 3 )
                                        dx =  (  CosP#( (Counters(0) + x)Mod 360 ) + SinP#( (Counters(1) + x)Mod 360) * CosP#( (Counters(2) + y)Mod 360) - SinP#( (Counters(3) + y)Mod 360)) * 40 
                                        dy =  (  SinP#( (Counters(4) + y)Mod 360 ) + CosP#( (Counters(5) + y)Mod 360) * SinP#( (Counters(6) + x)Mod 360) - CosP#( (Counters(7) + x)Mod 360)) * 40
                                EndIf
                                
                                ; keep the distortion inside the screen
                                If (dx < 0) Then dx = Abs(dx)
                                If (dy < 0) Then dy = Abs(dy)
                                If (dx >= gfx_width) Then dx = dx Mod gfx_width
                                If (dy >= gfx_height) Then dy = dy Mod gfx_height
                                
                                
                                If (x > 3)
                                        If (x < gfx_width-4)
                                                If (y> 3)
                                                        If (y < gfx_height-4)
                                                                WritePixelFast( x,y, screenMap( x, y ) )
                                                        EndIf
                                                EndIf
                                        EndIf
                                EndIf
                                
                                ; current pixel
                                r1 = (screenMap( x, y ) Shr 16) And $FF
                                g1 = (screenMap( x, y ) Shr  8) And $FF
                                b1 = (screenMap( x, y )       ) And $FF
                                
                                ; distortion pixel for feedback
                                r2 = (screenMap( dx, dy ) Shr 16) And $FF
                                g2 = (screenMap( dx, dy ) Shr  8) And $FF
                                b2 = (screenMap( dx, dy )       ) And $FF
                                
                                ; Alpha 50% + 50% has blurring effect
                                r2 = (r1 + r2) Shr 1
                                g2 = (g1 + g2) Shr 1
                                b2 = (b1 + b2) Shr 1        
                                
                                screenMap( x, y ) = r2 Shl 16 + g2 Shl 8 + b2
                        Next
                Next
                UnlockBuffer( BackBuffer() )
                
                
                ; here you give it color info, e.g i'm just creating a band of
                ; colors around the edges of the screen, you could say use the bass
                ; plugin and have equalizer lines accross the middle, this would
                ; then be the source of your colors
                
                ; fill top and bottom of screen with spectrum colors
                For y = 0 To gfx_heightM1
                        For z = 0 To 3
                                ci = ( colcount + y * 4 ) Mod 1536
                                screenMap( z, y ) = Spec( ci )
                                screenMap( gfx_widthM1-z , y ) = Spec( ci )
                        Next
                Next 
                        
                ; fill left and right of screen with spectrum colors        
                For x = 0 To gfx_widthM1
                        For z = 0 To 3
                                ci = ( colcount + ( x * 4 ) + gfx_heightM1 ) Mod 1536
                                screenMap( x, z ) = Spec( ci )
                                screenMap( x, gfx_heightM1-z  ) = Spec( ci )
                        Next
                Next 
                colcount = ( colcount + ccSpeed ) Mod 1536
                
        Flip
        Wend

        End ;Stop program
        
;===========================================================



;-----------------------------------------------------------
; Create a spectum of colors at full saturation

Function Spectrum()
;-----------------------------------------------------------

        Local rgb[3]
        rgb[0]=255

        Local seq[6]
        
        ; r g b change sequence
        seq[0] = 3
        seq[1] = 1
        seq[2] = 2
        seq[3] = 4
        seq[4] = 0
        seq[5] = 5
        
    count = 0
        For i = 0 To 5 
                inx = seq[ i ]
                For j = 0 To 255
                        If ( inx < 3 )
                                If ( rgb[inx] < 255 ) Then rgb[inx] = rgb[inx]+1        
                        Else
                                If ( rgb[inx Mod 3] > 0 ) Then rgb[inx Mod 3] = rgb[inx Mod 3] - 1        
                        EndIf
                        Spec(count) = rgb[0] Shl 16 + rgb[1] Shl 8 + rgb[2]
                        count=count+1
                Next
        Next
        
End Function


;-----------------------------------------------------------
; initialize counters

Function initializeCounters()
;----------------------------------------------------------

        SeedRnd MilliSecs()
        
        For i = 0 To 7
                Frequency( i ) = Rand( -5, 5 )
                
                While Frequency( i ) = 0
                        Frequency( i ) = Rand( -5, 5 )
                Wend
                
                Counters( i ) = Rand( 0 ,359 )
        Next
        ccSpeed = Rand( 1, 20 )
        colcount = Rand( 0, 1535 )
        Mode = Mode + 1
        If Mode > MaxMode Then Mode = 0
End Function


;-----------------------------------------------------------
; increment counters

Function inc_Counters()
;-----------------------------------------------------------

        For i = 0 To 7
                Counters(i) = Counters(i) + ( Frequency(i) )
                If Counters(i) > 360 Then Counters(i) = Counters(i) Mod 360
                If Counters(i) < 360 Then Counters(i) = Counters(i) + 360
        Next
        
End Function
;~IDEal Editor Parameters:
;~C#Blitz3D